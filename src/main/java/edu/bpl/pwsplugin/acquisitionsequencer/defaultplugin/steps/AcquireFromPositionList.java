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

package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.AcquireFromPositionListFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.IteratingContainerStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import java.util.List;
import java.util.concurrent.Callable;
import org.micromanager.AutofocusPlugin;
import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class AcquireFromPositionList
      extends IteratingContainerStep<AcquireFromPositionListFactory.AcquirePositionsSettings> {

   //Executes `step` at each position in the positionlist and increments the cell number each time.
   private Integer currentIteration = 0;

   public AcquireFromPositionList() {
      super(new AcquireFromPositionListFactory.AcquirePositionsSettings(),
            DefaultSequencerPlugin.Type.POS.name());
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      PositionList list = this.getSettings().posList;
      SequencerFunction stepFunction = super.getSubstepsFunction(callbacks);
      return (status) -> {
         //set timeout to 30 seconds. Otherwise we get an error if a position move takes greater than 5 seconds. (default timeout)
         Globals.core().setTimeoutMs(30000);
         currentIteration = 0;
         for (int i = 0; i < list.getNumberOfPositions(); i++) {
            currentIteration++;
            MultiStagePosition pos = list.getPosition(i);
            status.coords().setIterationOfCurrentStep(i);
            String label = pos.getLabel();
            status.newStatusMessage(String.format("Moving to position %s", label));
            TranslationStage1d zStage = Globals.getHardwareConfiguration().getActiveConfiguration().zStage();
            Callable<Void> preMoveRoutine = () -> {
               return null;
            };
            Callable<Void> postMoveRoutine = () -> {
               return null;
            };
            if (label.contains("-APFS-")) {
               //Turn off pfs before moving. after moving run autofocus to get back i the right range. then enable pfs again.
               preMoveRoutine = () -> {
                  Globals.core().enableContinuousFocus(false);
                  return null;
               };
               postMoveRoutine = () -> {
                  PFSFuncs.autoFocusThenPFS();
                  return null;
               };
            } else if (label.contains("-ZPFS-")) {
               //Turn off pfs, move, reenable pfs. make sure to set a coordinate for z-nonpfs for this to work.
               preMoveRoutine = () -> {
                  Globals.core().enableContinuousFocus(false);
                  return null;
               };
               postMoveRoutine = () -> {
                  PFSFuncs.pauseThenPFS();
                  return null;
               };
            } else if (label.contains("-PFS-")) {
               //If the position name has PFS then turn on pfs for this acquisition and then turn off.
               postMoveRoutine = () -> {
                  PFSFuncs.alignPFS();
                  return null;
               };
            } else if (label.contains("-ESC-")) {
               preMoveRoutine = () -> {
                  zStage.setEscaped(true);
                  return null;
               };

               postMoveRoutine = () -> {
                  zStage.setEscaped(false);
                  return null;
               };
            }
            preMoveRoutine.call();
            //Yes, I know this is weird. It's a static method that needs a position and the core as input.
            MultiStagePosition.goToPosition(pos, Globals.core());
            postMoveRoutine.call();
            status = stepFunction.apply(status);
            //Just in case the substep took us to new positions we want to make sure to move back to our position to avoid confusion.
            MultiStagePosition.goToPosition(pos, Globals.core());
         }
         currentIteration = 0;
         return status;
      };
   }

   @Override
   protected SimFn getSimulatedFunction() {
      SimFn subStepSimFn = this.getSubStepSimFunction();
      return (Step.SimulatedStatus status) -> {
         int iterations = this.settings.posList.getNumberOfPositions();
         for (int i = 0; i < iterations; i++) {
            status = subStepSimFn.apply(status);
         }
         return status;
      };
   }

   @Override
   public List<String> validate() {
      List<String> errs = super.validate();
      if (this.getSettings().posList.getNumberOfPositions() == 0) {
         errs.add(String.format("Position list for \"%s\" is empty.", this.toString()));
      }
      return errs;
   }

   @Override
   public Integer getCurrentIteration() {
      return currentIteration;
   }

   @Override
   public Integer getTotalIterations() {
      return settings.posList.getNumberOfPositions();
   }
}


class PFSFuncs {
   // TODO use the TranslationStage1D api to do this rather than going straight to the core.
   static void alignPFS() throws Exception {
      if (Globals.core().isContinuousFocusEnabled()) {
         Globals.core().enableContinuousFocus(true);
         Thread.sleep(3000);
         Globals.core().enableContinuousFocus(false);
      }
   }

   static void autoFocusThenPFS() throws Exception {
      AutofocusPlugin afPlugin = Globals.mm().getAutofocusManager().getAutofocusMethod();
      afPlugin.fullFocus(); //This blocks until the focus is done
      Thread.sleep(2000);
      Globals.core().enableContinuousFocus(true);
      Thread.sleep(3000);
   }

   static void pauseThenPFS() throws Exception {
      Thread.sleep(1000);
      Globals.core().enableContinuousFocus(true);
      Thread.sleep(3000);
   }
}
