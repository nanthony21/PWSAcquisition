/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import java.util.List;
import jdk.nashorn.internal.objects.Global;
import org.micromanager.data.Coords;

/**
 *
 * @author nick
 */
public class ZStackStep extends IteratingContainerStep<SequencerSettings.ZStackSettings> {
    private Integer currentIteration = 0;
    
    public ZStackStep() {
        super(new SequencerSettings.ZStackSettings(), SequencerConsts.Type.ZSTACK);
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
                    //Globals.core().setPosition(settings.deviceName, settings.startingPosition);
                    zStage.setPosUm(settings.startingPosition);
                }
                //double initialPos = Globals.core().getPosition(settings.deviceName);
                double initialPos = zStage.getPosUm();
                for (currentIteration = 0; currentIteration < settings.numStacks; currentIteration++) {
                    status.coords().setIterationOfCurrentStep(currentIteration); //Update the coordinates to indicate which iteration of this step we are on.
                    status.newStatusMessage(String.format("Moving to z-slice %d of %d", currentIteration + 1, settings.numStacks));
                    zStage.setPosUm(initialPos + (settings.intervalUm * currentIteration));
                    status = subStepFunc.apply(status);
                }
                zStage.setPosUm(initialPos); //Make sure to return to the initial position before finishing.
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
