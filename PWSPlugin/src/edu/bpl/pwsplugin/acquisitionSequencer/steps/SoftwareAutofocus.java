/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;

/**
 *
 * @author nick
 */
public class SoftwareAutofocus extends EndpointStep<SequencerSettings.SoftwareAutoFocusSettings> {
    
    public SoftwareAutofocus() {
        super(new SequencerSettings.SoftwareAutoFocusSettings(), SequencerConsts.Type.AF);
    }

    @Override
    public SequencerFunction getStepFunction() {
        SequencerSettings.SoftwareAutoFocusSettings settings = this.settings;
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                Globals.mm().getAutofocusManager().setAutofocusMethodByName(settings.afPluginName);
                Globals.mm().getAutofocusManager().getAutofocusMethod().fullFocus();
                return status;
            }
        };
    }

    @Override
    protected SimFn getSimulatedFunction() {
        return (Step.SimulatedStatus status) -> {
            return status;
        };
    }
    
}
