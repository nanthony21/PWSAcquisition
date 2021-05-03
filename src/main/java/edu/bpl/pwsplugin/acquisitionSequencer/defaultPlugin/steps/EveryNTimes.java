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
package edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import java.util.List;

/**
 *
 * @author nick
 */
public class EveryNTimes extends ContainerStep<SequencerSettings.EveryNTimesSettings> {
    
    int iteration = 0;
    int simulatedIteration = 0;

    public EveryNTimes() {
        super(new SequencerSettings.EveryNTimesSettings(), DefaultSequencerPlugin.Type.EVERYN.name());
    }

    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
        SequencerFunction stepFunction = super.getSubstepsFunction(callbacks);
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
