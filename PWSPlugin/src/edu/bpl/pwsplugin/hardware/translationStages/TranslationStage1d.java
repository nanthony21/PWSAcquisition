/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.beans.PropertyChangeListener;
import mmcorej.DeviceType;



/**
 *
 * @author nicke
 */
public abstract class TranslationStage1d {
    
    //protected abstract double convertValueToUm(double val) throws MMDeviceException;
    
    //protected abstract double convertUmToValue(double um) throws MMDeviceException;
    
    
    public abstract double getPosUm() throws MMDeviceException;
    
    public abstract void setPosUm(double um) throws MMDeviceException;
    
    public abstract boolean hasAutoFocus();
    
    public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
    
    public boolean getAutoFocusEnabled() throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
    
    public void addFocusLockListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException();
    }
     
    public enum Types {
        NikonTI,
        Simulated;
    }
        
    public static TranslationStage1d getInstance(TranslationStage1dSettings settings) {
        if (settings.stageType == TranslationStage1d.Types.NikonTI) {
            return new NikonTI1d(settings);
        } else if (settings.stageType == TranslationStage1d.Types.Simulated) {
            return new SimulationStage1d(settings);
        } else {
            return null; //This shouldn't ever happen.
        }
    }
    
    public static TranslationStage1d getAutomaticInstance() {
        //Detect which stage is connected automatically, assumes that only one is connected.
        return StageFactory.detectConnectedStage();
    }
}


class StageFactory {
    public static TranslationStage1d detectConnectedStage() {
        for (String devName : Globals.core().getLoadedDevicesOfType(DeviceType.StageDevice)) {
            TranslationStage1dSettings settings = new TranslationStage1dSettings();
            settings.deviceName = devName;
            String library;
            try {
                library = Globals.core().getDeviceLibrary(devName);
            } catch (Exception e) {
                Globals.mm().logs().logError(e);
                continue;
            }
            if (library.equals("DemoCamera")) {
                settings.stageType = TranslationStage1d.Types.Simulated;
                return new SimulationStage1d(settings);
            } else if (library.equals("NikonTI") || library.equals("NikonTI2")) { //TODO is the library name correct? can the same class handle the TI1 and the TI2?
                settings.stageType = TranslationStage1d.Types.NikonTI;
                return new NikonTI1d(settings);
            } else {
                Globals.mm().logs().logMessage(String.format("Detected unsupport translation stage %s from library %s", devName, library));
            }
        }
        return null; // no stages detected
    }
}
