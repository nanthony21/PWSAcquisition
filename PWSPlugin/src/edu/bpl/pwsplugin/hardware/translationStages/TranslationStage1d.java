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
import java.beans.PropertyChangeListener;
import mmcorej.DeviceType;



/**
 *
 * @author nicke
 */
public abstract class TranslationStage1d implements Device {
    @Override
    public void activate() {} //Nothing to do here //TODO set as z device, set as autofocus device.
    
    @Override
    public void initialize() {} //Nothing to do here 
    
    public abstract double getPosUm() throws MMDeviceException;
    
    public abstract void setPosUm(double um) throws MMDeviceException, InterruptedException;
    
    public abstract void setPosRelativeUm(double um) throws MMDeviceException, InterruptedException;
    
    public abstract boolean hasAutoFocus();
    
    //The following only need to be implemented if `hasAutoFocus` is true
    public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
    
    public boolean getAutoFocusEnabled() throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
    
    public boolean getAutoFocusLocked() throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
    
    public void runFullFocus() throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
    
    public void addFocusLockListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException();
    }
     
    public enum Types {
        NikonTI,
        Simulated,
        NikonTI2;
    }
        
    public static TranslationStage1d getInstance(TranslationStage1dSettings settings) {
        if (null == settings.stageType) {
            throw new RuntimeException("This shouldn't ever happen.");
        } else switch (settings.stageType) {
            case NikonTI:
                try {
                    return new NikonTI_zStage(settings);
                } catch (MMDeviceException e) {
                    Globals.mm().logs().logError(e);
                    return null;
                }
            case NikonTI2:
                try {
                    return new NikonTI2_zStage(settings);
                } catch (MMDeviceException e) {
                    Globals.mm().logs().logError(e);
                    return null;
                } 
            case Simulated:
                return new SimulationStage1d(settings);
            default:
                return null; //This shouldn't ever happen.
        }
    }
    
    public static TranslationStage1d getAutomaticInstance() {
        //Detect which stage is connected automatically, assumes that only one is connected.
        for (String devLabel : Globals.core().getLoadedDevicesOfType(DeviceType.StageDevice)) {
            TranslationStage1dSettings settings = new TranslationStage1dSettings();
            settings.deviceName = devLabel;
            String library;
            String name;
            try {
                library = Globals.core().getDeviceLibrary(devLabel);
                name = Globals.core().getDeviceName(devLabel);
            } catch (Exception e) {
                Globals.mm().logs().logError(e);
                continue;
            } //TODO make use of `identify` here. identify should be static.
            if (library.equals("DemoCamera")) {
                settings.stageType = TranslationStage1d.Types.Simulated;
                return new SimulationStage1d(settings);
            } else if ((library.equals("NikonTI") || library.equals("NikonTI2")) && name.equals("TIZDrive")) { //TODO is the library name correct? can the same class handle the TI1 and the TI2?
                settings.stageType = TranslationStage1d.Types.NikonTI;
                try {
                    return new NikonTI_zStage(settings);
                } catch (MMDeviceException e) {
                    Globals.mm().logs().logError(e);
                    return null;
                }
            }
        }
        return null; // no stages detected
    }
}
