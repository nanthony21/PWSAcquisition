/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AutoshutterSettings;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;

/**
 *
 * @author nick
 */
public class AutoShutter extends ContainerStep {
    
    @Override
    public SequencerFunction getFunction() {
        SequencerFunction stepFunction = super.getFunction();
        AutoshutterSettings settings = (AutoshutterSettings) this.getSettings();
        Illuminator illuminator = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.imagingConfigName).illuminator();//((AutoshutterSettings) this.getSettings()).illuminatorSettings //TODO how to get the illuminator?
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                //AUTOSHUTTER A function that turns on the lamp, waits `delay` seconds, runs the acquisitionHandle, then turns off the lamp.
                illuminator.setShutter(true);
                status.update("Delaying acquisition while lamp warms up.", status.currentCellNum);
                Thread.sleep((long)(settings.delaySeconds*1000));
                status = stepFunction.apply(status);
                illuminator.setShutter(false);
                return status;
            } 
        };
    }
   
}
