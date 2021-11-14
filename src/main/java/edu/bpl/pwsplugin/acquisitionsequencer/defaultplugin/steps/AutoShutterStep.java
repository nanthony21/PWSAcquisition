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
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import java.util.List;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class AutoShutterStep extends ContainerStep<SequencerSettings.AutoShutterSettings> {
   public AutoShutterStep() {
      super(new SequencerSettings.AutoShutterSettings(),
            DefaultSequencerPlugin.Type.AUTOSHUTTER.name());
   }

   @Override
   public boolean isRunning() {
      return false;
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      SequencerFunction subStepFunction = super.getSubstepsFunction(callbacks);
      return (status) -> {
         Illuminator illum =
               Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.configName)
                     .illuminator();
         status.newStatusMessage(
               String.format("Enabling illuminator for config: %s", settings.configName));
         illum.setShutter(true);

         long startTime = System.currentTimeMillis();
         int msgId = -1;
         String oldMsg = "";
         while ((System.currentTimeMillis() - startTime) / 60000.0 < settings.warmupTimeMinutes) {
            //Wait for the warmup time to expire.
            Thread.sleep(1000); //This pause allows the thread to be cancelled
            String msg = String.format(
                  "Illuminator warming up. Waiting %.1f minutes before proceeding.",
                  settings.warmupTimeMinutes - ((System.currentTimeMillis() - startTime)
                        / 60000.0));
            if (!msg.equals(oldMsg)) {
               if (msgId == -1) {
                  msgId = status.newStatusMessage(msg);
               } else {
                  status.updateStatusMessage(msgId, msg);
               }
               oldMsg = msg;
            }
         }

         status = subStepFunction.apply(status);
         status.newStatusMessage(
               String.format("Disabling illuminator for config: %s", settings.configName));
         illum.setShutter(false);
         return status;
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
}
