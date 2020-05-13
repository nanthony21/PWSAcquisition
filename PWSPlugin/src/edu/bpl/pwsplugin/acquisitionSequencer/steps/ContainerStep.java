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
public abstract class ContainerStep extends Step {
    //A `Step` that takes other `Step`s and wraps functionality around them.
    private Step step;
    
    public ContainerStep(SequencerSettings settings, Step subStep) {
        super(settings);
        this.setSubStep(subStep);
    }
    
    public Step getSubStep() {
        return this.step;
    }
    
    public void setSubStep(Step step) {
        this.step = step;
    }
}
