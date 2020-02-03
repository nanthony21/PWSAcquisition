/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class HamamatsuOrcaFlash4v3 extends Camera{
    
    @Override
    public boolean supportsExternalTriggering() { return true; }
    
    @Override
    public void configureExternalTriggering(boolean enable); //Turn external triggering on or off.
    
    @Override
    public boolean supportsTriggerOutput() { return true; }
    
    @Override
    public void configureTriggerOutput(boolean enable) {
        if (enable) {
            Globals.core().setProperty(devName, "OUTPUT TRIGGER SOURCE[0]", "READOUT END");
            Globals.core().setProperty(devName, "OUTPUT TRIGGER POLARITY[0]", "POSITIVE");
            Globals.core().setProperty(devName, "OUTPUT TRIGGER KIND[0]", "PROGRAMMABLE");
            Globals.core().setProperty(devName, "OUTPUT TRIGGER PERIOD[0]", 0.001); //The default is shorter than this and it is often missed by other devices.
        } else {
            Globals.core().setProperty(devName, "OUTPUT TRIGGER KIND[0]", "LOW");
        }
    }
    
    @Override
    public String getName(); //Get the device name used in Micro-Manager.
    
    @Override
    public List<String> valideate();
}
