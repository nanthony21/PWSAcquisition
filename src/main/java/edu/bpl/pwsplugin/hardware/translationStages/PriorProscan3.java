/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nicke
 */
public class PriorProscan3 extends TranslationStage1d {
    
    public  PriorProscan3(TranslationStage1dSettings settings) throws Device.IDException {
        super(settings);
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
            Globals.core().setPosition(settings.deviceName, um);
            Globals.core().waitForDevice(settings.deviceName);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean hasAutoFocus() { return false; }
    
    @Override
    public boolean identify() {
        try {
            return ((Globals.core().getDeviceName(settings.deviceName).equals("ZStage"))
                    &&
                    (Globals.core().getDeviceLibrary(settings.deviceName).equals("Prior")));
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        if (!identify()) {
            errs.add(String.format("Device %s is not recognized as a Prior ProScanIII Z-stage", settings.deviceName));
        }
        return errs;
    }
}
