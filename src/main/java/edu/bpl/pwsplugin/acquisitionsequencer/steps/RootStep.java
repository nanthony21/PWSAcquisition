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

package edu.bpl.pwsplugin.acquisitionsequencer.steps;

import com.google.gson.Gson;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;


/**
 * Represents the mandatory initial step of the experiment. Sets the root save path and other parameters.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class RootStep extends ContainerStep<SequencerSettings.RootStepSettings> {

   public RootStep() {
      super(new SequencerSettings.RootStepSettings(), SequencerConsts.Type.ROOT.name());
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
            Globals.logger().setAcquisitionPath(startingDir
                  .toPath()); //Begin saving additional log files to the acquisition directory.
            status.setAcquisitionNum(0);
            status.setSavePath(settings.directory);
            status.getRuntimeSettings()
                  .saveToJson(status.getSavePath()); //Save the runtimesettings to a JSON file.
            try {
               status = subStepFunc.apply(status);
            } catch (RuntimeException e) {
               Throwable exc = e.getCause();
               if (exc instanceof Exception) {
                  if (exc instanceof InterruptedException) {
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
               Globals.logger()
                     .closeAcquisition(); //Close the additional log files saved to the acquisition directory.
            }
            return status;
         }
      };
   }

   public final List<String> getRequiredPaths() {
      Step.SimulatedStatus status = new Step.SimulatedStatus();
      status.cellNum =
            0; //This number is incremented before acquisition so Cell1 is always the first one.
      status.workingDirectory = this.settings.directory;
      return this.getSimulatedFunction().apply(status).requiredPaths;
   }

   @Override
   protected SimFn getSimulatedFunction() {
      SimFn subStepSimFn = this.getSubStepSimFunction();
      return (Step.SimulatedStatus status) -> {
         status.requiredPaths.add(Paths.get(status.workingDirectory, "sequence.rtpwsseq")
               .toString()); //This way we get a warning about overwriting the sequence file.
         status = subStepSimFn.apply(status);
         return status;
      };
   }

   @Override
   public List<String> validate() {
      List<String> errs = super.validate();

      if (!Files.exists(Paths.get(this.settings.directory)) || (this.settings.directory.length()
            == 0)) {
         errs.add(String.format("File path not valid: %s", settings.directory));
      }
      if (this.settings.author.isEmpty()) {
         errs.add("`Author` field is blank.");
      }
      if (this.settings.project.isEmpty()) {
         errs.add("`Project` field is blank.");
      }
      if (this.settings.cellLine.isEmpty()) {
         errs.add("`Cell Line` field is blank.");
      }

      errs.addAll(this.validateSubfolderSteps());
      return errs;
   }

   public void saveToJson(String savePath) throws IOException {
      if (!savePath.endsWith(".pwsseq")) {
         savePath = savePath + ".pwsseq"; //Make sure the extension is there.
      }
      try (FileWriter writer = new FileWriter(
            savePath)) { //Writer is automatically closed at the end of this statement.
         Gson gson = GsonUtils.getGson();
         String json = gson.toJson(this);
         writer.write(json);
      }
   }

   private List<String> validateSubfolderSteps() {
      //Make sure that we don't have multiple "EnterSubFolderSteps" for the same subfolder.
      List<String> errs = new ArrayList<>();
      //Collect all subfolder steps.
      List<Step> subfolderSteps = new ArrayList<>();
      Enumeration<Step> en = (Enumeration<Step>) this.breadthFirstEnumeration();
      while (en.hasMoreElements()) {
         Step step = en.nextElement();
         if (step.getType().equals(SequencerConsts.Type.SUBFOLDER)) {
            subfolderSteps.add(step);
         }
      }

      //Build a list of all paths relative to the root.
      List<String> usedPaths = new ArrayList<>();
      for (Step<?> endPointStep : subfolderSteps) {
         TreeNode[] path = endPointStep.getPath(); //The path from the step up to the root
         Step<?>[] treePath = Arrays.copyOf(path, path.length, Step[].class); //cast to Step[].
         List<String> subfoldersAlongPath = new ArrayList<>();
         for (Step<?> step : treePath) {
            if (step.getType().equals(SequencerConsts.Type.SUBFOLDER)) {
               String relPath = ((SequencerSettings.EnterSubfolderSettings) step
                     .getSettings()).relativePath;
               subfoldersAlongPath.add(relPath);
            }
         }
         String fullPath = Paths
               .get("", subfoldersAlongPath.toArray(new String[subfoldersAlongPath.size()]))
               .toString();
         if (usedPaths.contains(fullPath)) {
            errs.add(String.format("Multiple `SubFolder` steps use path: %s", fullPath));
         }
         usedPaths.add(fullPath);
      }
      return errs;
   }
}