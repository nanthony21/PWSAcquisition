/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author nick
 */
public abstract class EndpointStep extends Step {
    //A `Step` which is an endpoint (does not support containing any substeps
    public EndpointStep(JsonableParam settings, Consts.Type type) {
        super(settings, type);
        this.setAllowsChildren(false);
    }
    
    public EndpointStep(EndpointStep step) {
        super(step);
        this.setAllowsChildren(false);
    }
    
    @Override
    public List<String> validate() {
        return new ArrayList<>(); //Assume that the endpoint steps are always valid. this can always be overridenn if this is not the case.
    }
    
    @Override
    protected void initializeSimulatedRun() {} //Most steps don't need to do anything.
}
