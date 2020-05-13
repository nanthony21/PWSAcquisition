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
public abstract class Step {
    private SequencerSettings settings; 

    public final SequencerSettings getSettings() { return settings; }
    public final void setSettings(SequencerSettings settings) { this.settings = settings; }
    public abstract SequencerFunction getFunction();
}
