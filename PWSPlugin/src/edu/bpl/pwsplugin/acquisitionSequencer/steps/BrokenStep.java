/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.List;

/**
 *
 * @author nick
 */
public class BrokenStep extends ContainerStep<JsonableParam> {
    public BrokenStep() {
        super(new JsonableParam(), SequencerConsts.Type.BROKEN);
    }
    
    @Override
    public SequencerFunction getStepFunction() {
        return (status) -> { throw new RuntimeException("The BrokenStep should never be run."); };
    }
    
    @Override
    public SimFn getSimulatedFunction() {
        return (status) -> { throw new RuntimeException("The BrokenStep should never be run."); };
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = super.validate();
        errs.add("The BROKEN step is caused by an error and can not be run. It must be replaced.");
        return errs;
    }   
}