/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;

/**
 *
 * @author nick
 */
public class SoftwareAutofocus extends EndpointStep {
    public SoftwareAutofocus(SoftwareAutoFocusSettings settings) {
        super(settings);
    }
    
    @Override
    public SequencerFunction getFunction() {
        SoftwareAutoFocusSettings settings = (SoftwareAutoFocusSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public Integer applyThrows(Integer cellNum) throws Exception {
                Globals.mm().getAutofocusManager().setAutofocusMethodByName(settings.afPluginName);
                Globals.mm().getAutofocusManager().getAutofocusMethod().fullFocus();
                return 0;
            } 
        };
    }
}
