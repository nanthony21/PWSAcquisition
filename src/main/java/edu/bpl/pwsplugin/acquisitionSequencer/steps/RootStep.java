///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;



/**
 *
 * @author nick
 */
public class RootStep extends ContainerStep<SequencerSettings.RootStepSettings> {

    public RootStep() {
        super(new SequencerSettings.RootStepSettings(), SequencerConsts.Type.ROOT);
    }
    
    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) { 
        SequencerSettings.RootStepSettings settings = this.settings;
        SequencerFunction subStepFunc = getSubstepsFunction(callbacks);
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                File startingDir = Paths.get(settings.directory).toFile();
                if (!startingDir.exists()) {
                    boolean success = startingDir.mkdirs();
                    if (!success) {
                        throw new IOException("Failed to create initial directory.");
                    }
                }
                Globals.logger().setAcquisitionPath(startingDir.toPath()); //Begin saving additional log files to the acquisition directory.
                status.setCellNum(0);
                status.setSavePath(settings.directory);
                TranslationStage1d zstage = Globals.getHardwareConfiguration().getActiveConfiguration().zStage();
                RootStep.this.saveToJson(Paths.get(settings.directory, "sequence.pwsseq").toString()); //Save the sequence to file for retrospect.
                try {
                    status = subStepFunc.apply(status);
                } catch (RuntimeException e) {
                    Throwable exc = e.getCause();
                    if (exc instanceof Exception) {
                        if (e.getCause() instanceof InterruptedException) {
                            status.newStatusMessage("User cancelled acquisition");
                            Globals.logger().logMsg("User cancelled acquisition");
                        } else {
                            Globals.logger().logError(e.getCause());
                        }
                    } else {
                        Globals.logger().logError(e);
                    }
                    throw e;
                } finally {
                    //Experiment is now finished
                    Globals.logger().closeAcquisition(); //Close the additional log files saved to the acquisition directory.
                }
                return status;
            }
        };    
    }
    
    public final List<String> getRequiredPaths() {
        Step.SimulatedStatus status = new Step.SimulatedStatus();
        status.cellNum = 0; //This number is incremented before acquisition so Cell1 is always the first one.
        status.workingDirectory = this.settings.directory;
        return this.getSimulatedFunction().apply(status).requiredPaths;
    }
    
    @Override
    protected SimFn getSimulatedFunction() {
        SimFn subStepSimFn = this.getSubStepSimFunction();
        return (Step.SimulatedStatus status) -> {
            status.requiredPaths.add(Paths.get(status.workingDirectory, "sequence.pwsseq").toString()); //This way we get a warning about overwriting the sequence file.
            status = subStepSimFn.apply(status);
            return status;
        };
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = super.validate();
        errs.addAll(this.validateSubfolderSteps());
        return errs;
    }
    
    private List<String> validateSubfolderSteps() {
        //Make sure that we don't have multiple "EnterSubFolderSteps" for the same subfolder.
        List<String> errs = new ArrayList<>();
        //Collect all subfolder steps.
        List<Step> subfolderSteps = new ArrayList<>();
        Enumeration<Step> en = (Enumeration<Step>) (Enumeration<? extends TreeNode>) this.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            Step step = en.nextElement();
            if (step.getType().equals(SequencerConsts.Type.SUBFOLDER)) {
                subfolderSteps.add(step);
            }
        }
        
        //Build a list of all paths relative to the root.
        List<String> usedPaths = new ArrayList<>();
        for (Step endPointStep : subfolderSteps) {
            TreeNode[] path = endPointStep.getPath(); //The path from the step up to the root
            Step[] treePath = Arrays.copyOf(path, path.length, Step[].class); //cast to Step[].
            List<String> subfoldersAlongPath = new ArrayList<>();
            for (Step step : treePath) {
                if (step.getType().equals(SequencerConsts.Type.SUBFOLDER)) {
                    String relPath = ((SequencerSettings.EnterSubfolderSettings) step.getSettings()).relativePath;
                    subfoldersAlongPath.add(relPath);
                }
            }
            String fullPath = Paths.get("", subfoldersAlongPath.toArray(new String[subfoldersAlongPath.size()])).toString();
            if (usedPaths.contains(fullPath)) {
                errs.add(String.format("Multiple `SubFolder` steps use path: %s", fullPath));
            }
            usedPaths.add(fullPath);
        }
        return errs;
    }
}