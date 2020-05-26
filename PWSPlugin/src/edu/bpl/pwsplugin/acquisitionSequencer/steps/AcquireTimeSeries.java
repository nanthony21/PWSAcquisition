/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;

/**
 *
 * @author nick
 */
public class AcquireTimeSeries extends ContainerStep {
    public AcquireTimeSeries() {
        super(Consts.Type.TIME);
    }
    
    @Override 
    public SequencerFunction getFunction() {
        SequencerFunction stepFunction = super.getFunction();
        AcquireTimeSeriesSettings settings = (AcquireTimeSeriesSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                //TIMESERIES execute acquisitionFunHandle repeatedly at a specified time
                //interval. the handle must take as input the Cell number to start at. It
                //will return the number of new acquisitions that it tood.
                double lastAcqTime = 0;
                for (int k=0; k<settings.numFrames; k++) {
                    // wait for the specified frame interval before proceeding to next frame
                    if (k!=0) { //No pause for the first iteration
                        int count = 0;
                        while ((System.currentTimeMillis() - lastAcqTime)/60000 < settings.frameIntervalMinutes) {
                            String msg = String.format("Waiting %.1f seconds before acquiring next frame", settings.frameIntervalMinutes - (System.currentTimeMillis() - lastAcqTime)/60000);
                            Globals.statusAlert().setText(msg);
                            count++;
                            Thread.sleep(500);
                        }   
                        if (count == 0) {
                            Globals.statusAlert().setText(String.format("Acquistion took %.1f seconds. Longer than the frame interval.", (System.currentTimeMillis() - lastAcqTime)/1000));
                        }
                    }
                    lastAcqTime = System.currentTimeMillis(); //Save the current time so we can figure out when to start the next acquisition.
                    status = stepFunction.apply(status);
                    status.update(String.format("Finished time set %d of %d", k, settings.numFrames), status.currentCellNum);
                }
                return status;
            }
        };
    }

    
    
}
