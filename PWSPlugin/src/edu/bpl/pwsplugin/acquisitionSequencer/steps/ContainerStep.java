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
public abstract class ContainerStep extends Step {
    //A `Step` that takes other `Step`s and wraps functionality around them.
    
    public ContainerStep(Consts.Type type) {
        super(type);
    }
    
    public ContainerStep(ContainerStep step) {
        super(step);
    }
    
    public ContainerStep(JsonableParam settings, Consts.Type type) {
        this(type);
        this.setSettings(settings);
    }
    
    
    public final List<Step> getSubSteps() {
        return Collections.list(this.children());
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
    
    //public abstract List<String> getEnclosedSavePaths();
    
    @Override
    public List<String> requiredRelativePaths(Integer startingCellNum) {
        Integer cellNum = startingCellNum;
        List<String> paths = new ArrayList<>();
        for (Step step : this.getSubSteps()) {
            paths.addAll(step.requiredRelativePaths(cellNum));
            cellNum += (int) Math.round(step.numberNewAcqs());
        }
        return paths;
    }
    
}
