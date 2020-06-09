/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author nick
 */
public abstract class ContainerStep<T extends JsonableParam> extends Step<T> {
    //A `Step` that takes other `Step`s and wraps functionality around them.
    
    public ContainerStep(T settings, Consts.Type type) {
        super(settings, type);
    }
    
    public ContainerStep(ContainerStep step) {
        super(step);
    }    
    
    public final List<Step> getSubSteps() {
        return Collections.list(this.children());
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        if (this.getSubSteps().isEmpty()) {
            errs.add(String.format("%s container-node may not be empty", this.toString()));
        }
        return errs;
    }
    
    protected final void addCallbackToSubsteps(SequencerFunction cb) { 
        //Add a callback for all the substeps of this step. Due to the implementation of `getSubstepsFunction` the callback will propagate all the way down.
        for (Step step : this.getSubSteps()) {
            step.addCallback(cb);
        }
    }
    
    public final SequencerFunction getSubstepsFunction() { // Execute each substep in sequence
        for (Step substep : this.getSubSteps()) { //Pass callbacks on to child steps.
            for (SequencerFunction cb : this.callbacks) {
                substep.addCallback(cb);
            }
        }
        List<SequencerFunction> stepFunctions = this.getSubSteps().stream().map(Step::getFunction).collect(Collectors.toList());
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                for (SequencerFunction func : stepFunctions) {
                    status = func.apply(status);
                }
                return status;   
            }
        };
    }
    
    @Override
    protected void initializeSimulatedRun() {
        //initialize all substeps;
        for (Step step : this.getSubSteps()) {
            step.initializeSimulatedRun();
        }
    }
}
