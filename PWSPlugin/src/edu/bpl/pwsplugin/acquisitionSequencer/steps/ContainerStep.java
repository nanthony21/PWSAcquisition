/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author nick
 */
public abstract class ContainerStep extends Step {
    //A `Step` that takes other `Step`s and wraps functionality around them.
    private List<Step> steps;
    
    public ContainerStep(Consts.Type type) {
        super(type);
    }
    
    public final List<Step> getSubSteps() {
        return this.steps;
    }
    
    public final void setSubSteps(List<Step> steps) {
        this.steps = steps;
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
        
    protected final Double numberNewAcqsOneIteration() { //The number of new acquisitions recursively from the substeps. Useful to reference from numberNewAcqs
        Double newAcqs = 0.0;
        for (Step substep : this.getSubSteps()) {
            newAcqs += substep.numberNewAcqs();
        }
        return newAcqs;
    }
    
        
    public final void addCallbackToSubsteps(SequencerFunction cb) { 
        for (Step step : this.getSubSteps()) {
            step.addCallback(cb);
        }
    }
}
