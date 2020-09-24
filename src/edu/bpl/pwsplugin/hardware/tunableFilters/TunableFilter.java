/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.illumination.SimulatedIlluminator;
import edu.bpl.pwsplugin.hardware.illumination.XCite120LED;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.function.Function;


/**
 *
 * @author N2-LiveCell
 */
public interface TunableFilter extends Device {
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
            
    /*public static TunableFilter getInstance(TunableFilterSettings settings) {
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
    }*/
    
    public static TunableFilter getAutomaticInstance(TunableFilterSettings settings) {
        Function<String, TunableFilterSettings> generator = (devName) -> {
            TunableFilterSettings sets = (TunableFilterSettings) settings.copy();
            sets.name = devName;
            return sets;
        };
        
        Device.AutoFinder<TunableFilter, TunableFilterSettings> finder = 
                new Device.AutoFinder<>(
                    TunableFilterSettings.class, 
                    generator,
                    VarispecLCTF.class,
                    KuriosLCTF.class,
                    SimulatedFilter.class
                );
                
        return finder.getAutoInstance(settings.name);
    }
    
    public enum Types {
        VARISPECLCTF,
        KURIOSLCTF,
        Simulated;
    }
}
