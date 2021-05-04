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

import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author nick
 */
public class EnterSubfolderStep extends ContainerStep<SequencerSettings.EnterSubfolderSettings> {

   private Integer cellNum = 0;
   private Integer simCellNum = 0;

   public EnterSubfolderStep() {
      super(new SequencerSettings.EnterSubfolderSettings(), SequencerConsts.Type.SUBFOLDER.name());
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      SequencerFunction stepFunction = super.getSubstepsFunction(callbacks);
      cellNum = 0; //initialize cell num
      SequencerSettings.EnterSubfolderSettings settings = this.settings;
      return new SequencerFunction() {
         @Override
         public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
            String origPath = status.getSavePath();
            Integer origCellNum = status.getAcquisitionlNum();
            status.newStatusMessage(
                  String.format("Moving to subfolder: %s", settings.relativePath));
            status.setSavePath(Paths.get(origPath).resolve(settings.relativePath).toString());
            status.setAcquisitionlNum(
                  cellNum); // Even if we exit and enter this subfolder multiple times we should still remember which cell num we're on.
            status = stepFunction.apply(status);
            cellNum =
                  status.getAcquisitionlNum(); //Update our placeholder with whatever we left off on.
            status.setSavePath(origPath);
            status.setAcquisitionlNum(origCellNum);
            return status;
         }
      };
   }

   @Override
   protected SimFn getSimulatedFunction() {
      SimFn subStepSimFn = this.getSubStepSimFunction();
      simCellNum = 0; //Initialize cell number.
      return (Step.SimulatedStatus status) -> {
         String path = this.settings.relativePath;
         Integer origCellNum = status.cellNum;
         String origDir = status.workingDirectory;
         status.cellNum =
               simCellNum; // Even if we exit and enter this subfolder multiple times we should still remember which cell num we're on.
         status.workingDirectory = Paths.get(status.workingDirectory, path).toString();
         status = subStepSimFn.apply(status);
         simCellNum = status.cellNum;
         status.workingDirectory = origDir;
         status.cellNum = origCellNum;
         return status;
      };
   }

   @Override
   public List<String> validate() {
      List<String> errs = super.validate();
      String path = this.settings.relativePath;
      if (path.equals("")) {
         errs.add("The `EnterSubFolder` path may not be empty.");
      }
      if (path.contains(".")) {
         errs.add("The `.` character is not allowed the in `EnterSubFolder` step.");
      }
      try {
         Paths.get(path);
      } catch (InvalidPathException e) {
         errs.add(String.format("Relative path %s is invalid", path));
      }
      return errs;
   }

   @Override
   public boolean isRunning() {
      return false;
   }

}
