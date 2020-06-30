/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
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
    //TODO more initialization: turn off PFS.
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
                status.setCellNum(0);
                status.setSavePath(settings.directory);
                RootStep.this.saveToJson(Paths.get(settings.directory, "sequence.pwsseq").toString()); //Save the sequence to file for retrospect.
                status = subStepFunc.apply(status);
                return status;
            }
        };    
    }
    
    public List<String> getRequiredPaths() {
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