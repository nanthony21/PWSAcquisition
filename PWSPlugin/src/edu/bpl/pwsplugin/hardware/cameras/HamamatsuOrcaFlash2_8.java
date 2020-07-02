/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nicke
 */
public class HamamatsuOrcaFlash2_8 extends DefaultCamera { 
    
    public HamamatsuOrcaFlash2_8(CamSettings settings) {
        super(settings);
    }

    @Override
    public boolean supportsExternalTriggering() { return false; }
        
    @Override
    public boolean supportsTriggerOutput() { return false; }
    
    @Override
    public void configureTriggerOutput(boolean enable) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The Flash 2.8 does not support output triggering");
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!Globals.core().getDeviceName(this.settings.name).equals("HamamatsuHam_DCAM")) { //TODO this is worthless for validation.
                errs.add(this.settings.name + " is not a HamamatsuHam_DCAM device");
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }

        return errs;
    }
    
}
