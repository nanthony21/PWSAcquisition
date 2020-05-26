/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;


/**
 *
 * @author nick
 */
public abstract class EndpointStep extends Step {
    //A `Step` which is an endpoint (does not support containing any substeps
    //TODO maybe this isn't needed
    public EndpointStep(Consts.Type type) {
        super(type);
    }
}
