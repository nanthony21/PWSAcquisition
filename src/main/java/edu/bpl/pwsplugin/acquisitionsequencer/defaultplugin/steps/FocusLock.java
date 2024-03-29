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
import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.FocusLockFactory.FocusLockSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class FocusLock extends ContainerStep<FocusLockSettings> {
   private boolean running = false;

   public FocusLock() {
      super(new FocusLockSettings(), DefaultSequencerPlugin.Type.PFS.name());
   }

   @Override
   protected SequencerFunction getCallback() {
      return (status) -> {
         running = true;
         //Indicates our current location in the tree of steps.
         Step[] path = status.coords().getTreePath();
         //If the current  step is an acquisition then check for refocus.
         if (path[path.length - 1].getType().equals(DefaultSequencerPlugin.Type.ACQ.name())) {
            TranslationStage1d zStage =
                  Globals.getHardwareConfiguration().getActiveConfiguration().zStage();
            if (!zStage.hasAutoFocus()) {
               status.newStatusMessage(
                     "Focus Lock: Error: The current zStage has no autofocus functionality.");
               return status;
            }
            if (!zStage.getAutoFocusLocked()) { //Check if focused. and log.
               status.newStatusMessage("Focus Lock: Focus is unlocked. Reacquiring focus.");
               try {
                  zStage.runFullFocus(); // This can fail and throw an exception, don't let that crash the whole experiment.
               } catch (MMDeviceException e) {
                  status.newStatusMessage("Focus Lock: Error: Focus lock failed to recover focus.");
               }
               Thread.sleep(
                     1000); //Without this we will sometime not actually re-enable pfs for some reason.
               zStage.setAutoFocusEnabled(true);
               Thread.sleep(
                     (long) (settings.delay * 1000.0)); //Does this actually serve any purpose?
            }
         }
         running = false;
         return status;
      };
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {

      SequencerFunction subStepFunction = super.getSubstepsFunction(callbacks);
      FocusLockSettings settings = this.getSettings();
      return (status) -> {
         running = true;
         //FocusLock A function that turns on the PFS, runs substep and then turns it off.
         TranslationStage1d zstage = Globals.getHardwareConfiguration().getActiveConfiguration().zStage();
         if (!zstage.getAutoFocusLocked()) {  // If Focus is already locked then we don't really need to do any initialization.
            try {
               status.newStatusMessage("Focus Lock: Finding initial focus.");
               double startingZ = zstage.getPosUm(); //After finding focus lock we will move back to this z position.
               zstage.runFullFocus();
               Thread.sleep(1000); //Without this we will sometimes not actually re-enable pfs for some reason.
               zstage.setAutoFocusEnabled(true);
               zstage.setPosUm(startingZ); //Move back to our starting position, except now PFS should be locked.
               Thread.sleep((long) (settings.delay * 1000.0));
            } catch (MMDeviceException e) {
               status.newStatusMessage("Focus Lock: Error: Focus lock failed to find initial focus.");
               zstage.setAutoFocusEnabled(false); //If we failed then make sure to completely disable autofocus.
            }
         }
         running = false;
         AcquisitionStatus newstatus = subStepFunction.apply(status);
         running = true;
         zstage.setAutoFocusEnabled(false);
         running = false;
         return newstatus;
      };
   }

   @Override
   protected SimFn getSimulatedFunction() {
      SimFn subStepSimFn = this.getSubStepSimFunction();
      return (Step.SimulatedStatus status) -> {
         status = subStepSimFn.apply(status);
         return status;
      };
   }

   @Override
   public List<String> validate() {
      List<String> errs = super.validate();

      if (!Globals.getHardwareConfiguration().getActiveConfiguration().zStage().hasAutoFocus()) {
         errs.add("Optical Focus Lock can not be used for a Z stage that does not support hardware autofocus.");
      }

      //Check that the focus lock doesn't contain any illegal steps such as another focus lock step
      Enumeration<Step> en =
            (Enumeration<Step>) (Enumeration<? extends TreeNode>) this.breadthFirstEnumeration();
      en.nextElement(); //This clears the first item which is just a reference to this very same step.
      while (en.hasMoreElements()) {
         Step step = en.nextElement();
         if (step.getType().equals(DefaultSequencerPlugin.Type.PFS.name())) {
            errs.add("Optical Focus Lock may not contain a sub-step of type: Optical Focus Lock");
         } else if (step.getType().equals(DefaultSequencerPlugin.Type.AF
               .name())) { //The autofocus step makes calls that move z without using our custom zStage devices, this will break the focus lock.
            errs.add("Optical Focus Lock may not contain a sub-step of type: Autofocus");
         }
      }
      return errs;
   }
}
