/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import java.util.List;

/**
 *
 * @author nicke
 */
public class AutoShutterStep extends ContainerStep<SequencerSettings.AutoShutterSettings> {
    public AutoShutterStep() {
        super(new SequencerSettings.AutoShutterSettings(), SequencerConsts.Type.AUTOSHUTTER);
    }

    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
        SequencerFunction subStepFunction = super.getSubstepsFunction(callbacks);
        return (status) -> {
            Illuminator illum = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.configName).illuminator();
            status.newStatusMessage(String.format("Enabling illuminator for config: %s", settings.configName));
            illum.setShutter(true);
            
            long startTime = System.currentTimeMillis();
            int msgId = -1;
            String oldMsg = "";
            while ((System.currentTimeMillis() - startTime) / 60000.0 < settings.warmupTimeMinutes) {
                //Wait for the warmup time to expire.
                String msg = String.format(
                        "Illuminator is warming up. Waiting %.1f minutes before proceeding.", 
                        (System.currentTimeMillis() - startTime) / 60000.0 - settings.warmupTimeMinutes);
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
            status.newStatusMessage(String.format("Disabling illuminator for config: %s", settings.configName));
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
