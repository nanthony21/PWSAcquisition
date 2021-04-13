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

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.IteratingContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import java.util.List;

/**
 *
 * @author nick
 */
public class ZStackStep extends IteratingContainerStep<SequencerSettings.ZStackSettings> {
    private Integer currentIteration = 0;
    
    public ZStackStep() {
        super(new SequencerSettings.ZStackSettings(), DefaultSequencerPlugin.Type.ZSTACK.name());
    }

    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
        SequencerSettings.ZStackSettings settings = this.getSettings();
        SequencerFunction subStepFunc = getSubstepsFunction(callbacks);
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                TranslationStage1d zStage = Globals.getHardwareConfiguration().getImagingConfigurations().get(0).zStage();
                if (settings.absolute) {
                    zStage.setPosUm(settings.startingPosition);
                }
                for (currentIteration = 0; currentIteration < settings.numStacks; currentIteration++) {
                    status.coords().setIterationOfCurrentStep(currentIteration); //Update the coordinates to indicate which iteration of this step we are on.
                    status.newStatusMessage(String.format("Moving to z-slice %d of %d", currentIteration + 1, settings.numStacks));
                    zStage.setPosRelativeUm(settings.intervalUm);
                    status = subStepFunc.apply(status);
                }
                zStage.setPosRelativeUm(-settings.intervalUm * settings.numStacks); //Make sure to return to the initial position before finishing. The reason we use relative movement is that in the case of a hardware autofocus (PFS) the absolute value may change, expecially if we have moved to difference XY positions.
                return status;
            }
        };
    }

    @Override
    protected SimFn getSimulatedFunction() {
        SimFn subStepSimFn = this.getSubStepSimFunction();
        return (Step.SimulatedStatus status) -> {
            int iterations = this.settings.numStacks;
            for (int i = 0; i < iterations; i++) {
                status = subStepSimFn.apply(status);
            }
            return status;
        };
    }
    
    @Override
    public Integer getCurrentIteration() {
        return currentIteration;
    }
    
    @Override
    public Integer getTotalIterations() {
        return settings.numStacks;
    }
    
}
