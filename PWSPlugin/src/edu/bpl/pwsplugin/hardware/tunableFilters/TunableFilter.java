/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.List;


/**
 *
 * @author N2-LiveCell
 */
public interface TunableFilter {
    public void setWavelength(int wavelength) throws MMDeviceException;
    
    public int getWavelength() throws MMDeviceException;
    
    public boolean supportsSequencing();
    
    public int getMaxSequenceLength() throws MMDeviceException;
    
    public void loadSequence(int[] wavelengthSequence) throws MMDeviceException;
    
    public void startSequence() throws MMDeviceException;
    
    public void stopSequence() throws MMDeviceException;
    
    public boolean isBusy() throws MMDeviceException;
    
    public double getDelayMs() throws MMDeviceException;
    
    public TunableFilterSettings getSettings();
    
    public boolean identify(); //return true if this class supports the device specified in the settings.
    
    public List<String> validate(); //Return a list of errors found with the device.
    
    public void initialize() throws MMDeviceException; // One time initialization of device
    
    public void activate() throws MMDeviceException; //Make sure this device is ready for usage, may be run many times.
    
    public static TunableFilter getInstance(TunableFilterSettings settings) {
        if (null == settings.filterType) {
            throw new RuntimeException("This shouldn't ever happen.");
        } else switch (settings.filterType) {
            case VARISPECLCTF:
                return new VarispecLCTF(settings);
            case KURIOSLCTF:
                return new KuriosLCTF(settings);
            case Simulated:
                return new SimulatedFilter(settings);
            default:
                return null; //This shouldn't ever happen.
        }
    }
    
    public enum Types {
        VARISPECLCTF,
        KURIOSLCTF,
        Simulated;
    }
}
