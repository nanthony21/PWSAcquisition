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
import java.io.File;
import java.io.IOException;
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
                File startingDir = Paths.get(settings.directory).toFile();
                if (!startingDir.exists()) {
                    boolean success = startingDir.mkdirs();
                    if (!success) {
                        throw new IOException("Failed to create initial directory.");
                    }
                }
                status.setCellNum(0);
                status.setSavePath(settings.directory);
                RootStep.this.saveToJson(Paths.get(settings.directory, "sequence.pwsseq").toString()); //Save the sequence to file for retrospect.
                status = subStepFunc.apply(status);
                return status;
            }
        };    
    }
    
    public List<String> getRequiredPaths() {
        Step.SimulatedStatus status = new Step.SimulatedStatus();
        status.cellNum = 0; //This number is incremented before acquisition so Cell1 is always the first one.
        status.workingDirectory = this.settings.directory;
        return this.getSimulatedFunction().apply(status).requiredPaths;
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