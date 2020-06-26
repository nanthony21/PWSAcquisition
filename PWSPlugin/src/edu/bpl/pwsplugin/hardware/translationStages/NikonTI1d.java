/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import mmcorej.DeviceType;

/**
 *
 * @author nicke
 */
public class NikonTI1d extends TranslationStage1d {
    private final TranslationStage1dSettings settings;
    private final double pfsConversion = .1; //TODO figure out actual value
    private String devName;

    public NikonTI1d(TranslationStage1dSettings settings) throws MMDeviceException {
        this.settings = settings;
        try {
        for (String devName : Globals.core().getLoadedDevicesOfType(DeviceType.StageDevice)) {
            String library = Globals.core().getDeviceLibrary(devName);
            if (library.equals("DemoCamera")) {////For now this just support simulation and nikon. should be split into separate classes.
                this.devName = devName;
                return;
            }
        }
        } catch (Exception e) {
            throw new MMDeviceException();
        }
        throw new MMDeviceException();
    }
    
    @Override
    protected double convertValueToUm(double val) throws MMDeviceException{
        if (this.getAutoFocusEnabled()) {
            return val * pfsConversion;
        } else {
            return val;
        }
    }
    
    @Override
    protected double convertUmToValue(double um) throws MMDeviceException {
        if (this.getAutoFocusEnabled()) {
            return um / pfsConversion;
        } else {
            return um;
        }     
    }
    
    @Override
    public void setPosUm(double um) throws MMDeviceException {
        try {
            if (this.getAutoFocusEnabled()) {
                double val = um / pfsConversion;
                Globals.core().setAutoFocusOffset(val);
            } else {
                Globals.core().setPosition(devName, um);
            }    
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }

    }
    
    @Override
    public double getPosUm() throws MMDeviceException {
        try {
            double val = Globals.core().getPosition(devName);
            return val;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }         
    }
    
    @Override
    public boolean hasAutoFocus() {
        return true;
    }
    
    @Override
    public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
        try {
            Globals.core().enableContinuousFocus(enable);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean getAutoFocusEnabled() throws MMDeviceException {
        try {
            return Globals.core().isContinuousFocusEnabled();
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
}
