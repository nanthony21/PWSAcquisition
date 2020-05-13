/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;

/**
 *
 * @author nick
 */
public class FocusLock extends ContainerStep {
    public FocusLock(FocusLockSettings settings, Step subStep) {
        super(settings, subStep);
    }
    
    @Override
    public SequencerFunction getFunction() {
        SequencerFunction stepFunction = this.getSubStep().getFunction();
        FocusLockSettings settings = (FocusLockSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public Integer applyThrows(Integer cellNum) throws Exception {
                //FocusLock A function that turns on the PFS, runs substep and then turns it off.
                Globals.core().setAutoFocusOffset(settings.zOffset);
                Globals.core().fullFocus();
                Globals.core().enableContinuousFocus(true);
                Thread.sleep((long)(settings.preDelay * 1000.0));
                int numOfNewAcqs = stepFunction.apply(cellNum);
                if (!Globals.core().isContinuousFocusLocked()) {
                    Globals.mm().logs().logMessage("Autofocus failed!");
                    Globals.statusAlert().setText("Autofocus failed!");
                }
                Globals.core().enableContinuousFocus(false);
                return numOfNewAcqs;
            } 
        };
    }
}
