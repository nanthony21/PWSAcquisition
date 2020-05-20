/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;

/**
 *
 * @author nick
 */
public class FocusLock extends ContainerStep {
    
    @Override
    public SequencerFunction getFunction() {
        SequencerFunction stepFunction = super.getFunction();
        FocusLockSettings settings = (FocusLockSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                //FocusLock A function that turns on the PFS, runs substep and then turns it off.
                Globals.core().setAutoFocusOffset(settings.zOffset);
                Globals.core().fullFocus();
                Globals.core().enableContinuousFocus(true);
                Thread.sleep((long)(settings.preDelay * 1000.0));
                AcquisitionStatus newstatus = stepFunction.apply(status);
                if (!Globals.core().isContinuousFocusLocked()) {
                    Globals.mm().logs().logMessage("Autofocus failed!");
                    Globals.statusAlert().setText("Autofocus failed!");
                }
                Globals.core().enableContinuousFocus(false);
                return newstatus;
            } 
        };
    }
}
