/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import java.nio.file.Paths;
import java.util.List;



/**
 *
 * @author nick
 */
public class RootStep extends ContainerStep<SequencerSettings.RootStepSettings> {
    public RootStep() {
        super(new SequencerSettings.RootStepSettings(), Consts.Type.ROOT);
    }
    
    @Override
    public SequencerFunction getStepFunction() { 
        SequencerSettings.RootStepSettings settings = this.settings;
        SequencerFunction subStepFunc = getSubstepsFunction();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                status.setCellNum(0);
                status.setSavePath(settings.directory);
                RootStep.this.saveToJson(Paths.get(settings.directory, "sequence.pwsseq").toString()); //Save the sequence to file for retrospect.
                status = subStepFunc.apply(status);
                return status;
            }
        };    
    }
    
    public List<String> getRequiredPaths() {
        this.initializeSimulatedRun();
        Step.SimulatedStatus status = new Step.SimulatedStatus();
        status.cellNum = 1;
        status.workingDirectory = this.settings.directory;
        return this.simulateRun(status).requiredPaths;
    }
    
    @Override
    protected Step.SimulatedStatus simulateRun(Step.SimulatedStatus status) {
        for (Step step : this.getSubSteps()) {
            status = step.simulateRun(status);
        }
        return status;
    }
}