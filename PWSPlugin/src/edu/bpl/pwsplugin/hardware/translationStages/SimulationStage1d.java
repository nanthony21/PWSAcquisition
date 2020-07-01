/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;

/**
 *
 * @author nicke
 */
public class SimulationStage1d extends TranslationStage1d {
    private final TranslationStage1dSettings settings;
    
    public  SimulationStage1d(TranslationStage1dSettings settings) {
        this.settings = settings;
    }
    
    @Override
    public double getPosUm() throws MMDeviceException {
        try {
            return Globals.core().getPosition(this.settings.deviceName);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public void setPosRelativeUm(double um) throws MMDeviceException {
        try {
            Globals.core().setRelativePosition(settings.deviceName, um); 
            Globals.core().waitForDevice(settings.deviceName);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }         
    }
    
    @Override
    public void setPosUm(double um) throws MMDeviceException {
        try {
            Globals.core().setPosition(this.settings.deviceName, um);
            Globals.core().waitForDevice(settings.deviceName);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean hasAutoFocus() { return false; }
}
