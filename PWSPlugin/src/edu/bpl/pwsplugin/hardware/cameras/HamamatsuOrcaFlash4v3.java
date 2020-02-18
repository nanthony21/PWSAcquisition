/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class HamamatsuOrcaFlash4v3 extends Camera{
    String _devName;
    PWSPluginSettings.HWConfiguration.CamSettings _settings;
    
    public HamamatsuOrcaFlash4v3(PWSPluginSettings.HWConfiguration.CamSettings settings) {
        _settings = settings;
        _devName = settings.name;
    }
    
    @Override
    public void initialize() throws Exception {        
        Globals.core().setProperty(this._devName, "TRIGGER SOURCE", "INTERNAL");
        Globals.core().setProperty(this._devName, "MASTER PULSE TRIGGER SOURCE", "SOFTWARE");
        Globals.core().setProperty(this._devName, "MASTER PULSE MODE", "CONTINUOUS");
        Globals.core().setProperty(this._devName, "OUTPUT TRIGGER SOURCE[0]", "READOUT END");
        Globals.core().setProperty(this._devName, "OUTPUT TRIGGER POLARITY[0]", "POSITIVE");
    }
    
    @Override
    public boolean supportsExternalTriggering() { return true; }
    
    @Override
    public void configureExternalTriggering(boolean enable, double delayMs) throws Exception { //Turn external triggering on or off.
        if (enable) {
            Globals.core().setProperty(this._devName, "TRIGGER SOURCE", "EXTERNAL");
            Globals.core().setProperty(this._devName, "TRIGGER DELAY", delayMs/1000); //This is in units of seconds.
        } else {
            Globals.core().setProperty(this._devName, "TRIGGER SOURCE", "MASTER PULSE");
            Globals.core().setProperty(this._devName, "TRIGGER DELAY", delayMs/1000); //This is in units of seconds.
        }
    }
    
    @Override
    public boolean supportsTriggerOutput() { return true; }
    
    @Override
    public void configureTriggerOutput(boolean enable) throws Exception {
        if (enable) {
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER SOURCE[0]", "READOUT END");
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER POLARITY[0]", "POSITIVE");
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER KIND[0]", "PROGRAMMABLE");
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER PERIOD[0]", 0.001); //The default is shorter than this and it is often missed by other devices.
        } else {
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER KIND[0]", "LOW");
        }
    }
    
    @Override
    public String getName() { //Get the device name used in Micro-Manager.
        return this._devName;
    }
    
    @Override
    public void startAcquisition(int numImages, double intervalMs) throws Exception{
        Globals.core().setProperty(this._devName, "TRIGGER SOURCE", "MASTER PULSE"); //Make sure that Master Pulse is triggering the camera.
        Globals.core().setProperty(this._devName, "MASTER PULSE INTERVAL", intervalMs/1000.0); //In units of seconds
        Globals.core().startSequenceAcquisition(numImages, 0, false); //The hamamatsu adapter throws an error if the interval is not 0.
    }
    
    public PWSPluginSettings.HWConfiguration.CamSettings getSettings() {
        return _settings;
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!Globals.core().getDeviceName(this._devName).equals("HamamatsuHam_DCAM")) {
                errs.add(_devName + " is not a HamamatsuHam_DCAM device");
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }
        return errs;
    }
}
