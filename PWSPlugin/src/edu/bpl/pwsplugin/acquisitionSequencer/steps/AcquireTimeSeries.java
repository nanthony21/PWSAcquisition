/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import org.micromanager.data.Coords;

/**
 *
 * @author nick
 */
public class AcquireTimeSeries extends ContainerStep<SequencerSettings.AcquireTimeSeriesSettings> {
    
    public AcquireTimeSeries() {
        super(new SequencerSettings.AcquireTimeSeriesSettings(), SequencerConsts.Type.TIME);
    }

    @Override
    public SequencerFunction getStepFunction() {
        SequencerFunction stepFunction = super.getSubstepsFunction();
        SequencerSettings.AcquireTimeSeriesSettings settings = this.settings;
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                //TIMESERIES execute acquisitionFunHandle repeatedly at a specified time
                //interval. the handle must take as input the Cell number to start at. It
                //will return the number of new acquisitions that it tood.
                double lastAcqTime = 0;
                for (int k = 0; k < settings.numFrames; k++) {
                    // wait for the specified frame interval before proceeding to next frame
                    status.coords().setIterationOfCurrentStep(k);
                    if (k != 0) {
                        //No pause for the first iteration
                        Integer msgId = status.newStatusMessage("Waiting"); //This will be updated below.
                        int count = 0;
                        while ((System.currentTimeMillis() - lastAcqTime) / 60000 < settings.frameIntervalMinutes) {
                            String msg = String.format("Waiting %.1f minutes before acquiring next frame", settings.frameIntervalMinutes - (System.currentTimeMillis() - lastAcqTime) / 60000);
                            status.updateStatusMessage(msgId, msg);
                            count++;
                            Thread.sleep(500);
                        }
                        if (count == 0) {
                            status.updateStatusMessage(msgId, String.format("Acquisition took %.1f seconds. Longer than the frame interval.", (System.currentTimeMillis() - lastAcqTime) / 1000));
                        }
                    }
                    lastAcqTime = System.currentTimeMillis(); //Save the current time so we can figure out when to start the next acquisition.
                    status = stepFunction.apply(status);
                    status.newStatusMessage(String.format("Finished time step %d of %d", k + 1, settings.numFrames));
                }
                return status;
            }
        };
    }

    @Override
    protected SimFn getSimulatedFunction() {
        SimFn subStepSimFn = this.getSubStepSimFunction();
        return (Step.SimulatedStatus status) -> {
            int iterations = this.settings.numFrames;
            for (int i = 0; i < iterations; i++) {
                status = subStepSimFn.apply(status);
            }
            return status;
        };
    }
    
}
