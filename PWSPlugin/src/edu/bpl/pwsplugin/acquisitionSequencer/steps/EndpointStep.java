/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;

/**
 *
 * @author nick
 */
public abstract class EndpointStep extends Step {
    //A `Step` which is an endpoint (does not contain any substeps
    public EndpointStep(SequencerSettings settings) {
        super(settings);
    }
}
