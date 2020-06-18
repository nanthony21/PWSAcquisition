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

/**
 *
 * @author nick
 */
public class EveryNTimes extends ContainerStep<SequencerSettings.EveryNTimesSettings> {
    
    int iteration = 0;
    int simulatedIteration = 0;

    public EveryNTimes() {
        super(new SequencerSettings.EveryNTimesSettings(), SequencerConsts.Type.EVERYN);
    }

    @Override
    public SequencerFunction getStepFunction() {
        SequencerFunction stepFunction = super.getSubstepsFunction();
        iteration = 0; //initialize
        SequencerSettings.EveryNTimesSettings settings = this.settings;
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                if (((iteration - settings.offset) % settings.n) == 0) {
                    status.newStatusMessage(String.format("EveryNTimes: Running substep on iteration %d", iteration+1));
                    status = stepFunction.apply(status);
                }
                iteration++;
                return status;
            }
        };
    }

    @Override
    protected SimFn getSimulatedFunction() {
        SimFn subStepSimFn = this.getSubStepSimFunction();
        simulatedIteration = 0; //Initialize
        return (Step.SimulatedStatus status) -> {
            if (((simulatedIteration - this.settings.offset) % this.settings.n) == 0) {
                status = subStepSimFn.apply(status);
            }
            simulatedIteration++;
            return status;
        };
    }
    
}
