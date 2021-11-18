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
import edu.bpl.pwsplugin.acquisitionsequencer.utility.SubfolderHelper;
import edu.bpl.pwsplugin.acquisitionsequencer.utility.SubfolderHelper.Simulated;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tells the sequencer to create and work within a new subfolder. The acquisition number is reset in the new folder.
 * @author Nick Anthony (nickmanthony@hotmail.com)
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
            status.newStatusMessage(
                  String.format("Moving to subfolder: %s", settings.relativePath));
            try (SubfolderHelper.Runtime helper = new SubfolderHelper.Runtime(status, settings.relativePath, cellNum)) {
               status = stepFunction.apply(status);
               cellNum = helper.getCurrentCellNumber(); //Update our placeholder with whatever we left off on.
            }
            return status;
         }
      };
   }

   @Override
   protected SimFn getSimulatedFunction() {
      SimFn subStepSimFn = this.getSubStepSimFunction();
      simCellNum = 0; //Initialize cell number.
      return (Step.SimulatedStatus status) -> {
         try (SubfolderHelper.Simulated helper = new Simulated(status, settings.relativePath, simCellNum)) {
            status = subStepSimFn.apply(status);
            simCellNum = helper.getCurrentCellNum();
         }
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
}
