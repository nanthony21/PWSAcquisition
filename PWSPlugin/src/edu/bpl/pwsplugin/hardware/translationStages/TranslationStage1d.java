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
public abstract class TranslationStage1d {
    
    protected abstract double convertValueToUm(double val) throws MMDeviceException;
    
    protected abstract double convertUmToValue(double um) throws MMDeviceException;
    
    
    public abstract double getPosUm() throws MMDeviceException;
    
    public abstract void setPosUm(double um) throws MMDeviceException;
    
    public abstract boolean hasAutoFocus();
    
    public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
    
    public boolean getAutoFocusEnabled() throws MMDeviceException {
        throw new UnsupportedOperationException();
    }
     
    public enum Types {
        NikonTI,
        Simulated;
    }
        
    public static TranslationStage1d getInstance(TranslationStage1dSettings settings) {
        try {
            if (settings.stageType == TranslationStage1d.Types.NikonTI) {
                return new NikonTI1d(settings);
            } else if (settings.stageType == TranslationStage1d.Types.Simulated) {
                return new NikonTI1d(settings);
            } else {
                return null; //This shouldn't ever happen.
            }
        } catch (MMDeviceException e) {
            Globals.mm().logs().logError(e);
            return null;
        }
    }
}
