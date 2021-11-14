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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class ChangeConfigGroup extends ContainerStep<SequencerSettings.ChangeConfigGroupSettings> {
   private boolean running = false;

   public ChangeConfigGroup() {
      super(new SequencerSettings.ChangeConfigGroupSettings(),
            DefaultSequencerPlugin.Type.CONFIG.name());
   }

   @Override
   public boolean isRunning() {
      return running;
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      SequencerFunction subStepFunc = getSubstepsFunction(callbacks);
      SequencerSettings.ChangeConfigGroupSettings settings = this.settings;
      return (status) -> {
         running = true;
         String origConfValue = Globals.core().getCurrentConfig(settings.configGroupName);
         status.newStatusMessage(
               String.format("Changing %s config group to %s", settings.configGroupName,
                     settings.configValue));
         Globals.core().setConfig(settings.configGroupName, settings.configValue);
         Globals.core().waitForConfig(settings.configGroupName, settings.configValue);
         running = false;
         status = subStepFunc.apply(status);
         running = true;
         if (settings.resetWhenFinished) {
            Globals.core().setConfig(settings.configGroupName, origConfValue);
            Globals.core().waitForConfig(settings.configGroupName, origConfValue);
            status.newStatusMessage(
                  String.format("Changing %s config group back to original setting, %s",
                        settings.configGroupName, origConfValue));
         }
         running = false;
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

   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      if (!Globals.core().isGroupDefined(settings.configGroupName)) {
         errs.add(String.format("Configuration group %s is not defined", settings.configGroupName));
      }
      if (!Globals.core().isConfigDefined(settings.configGroupName, settings.configValue)) {
         errs.add(String.format("Value %s for configuration group %s is not defined", settings.configValue, settings.configGroupName));
      }
      return errs;
   }

}
