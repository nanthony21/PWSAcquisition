package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.function.Function;


public interface Illuminator extends Device {

    public void setShutter(boolean on) throws MMDeviceException;
    
    /*public static Illuminator getInstance(IlluminatorSettings settings) {
        if (null == settings.illuminatorType) {
            throw new RuntimeException("Programming Error: Illumator type " + settings.illuminatorType + " is not supported.");
        } else switch (settings.illuminatorType) {
            case XCite120LED:
                return new XCite120LED(settings);
            case Simulated:
                return new SimulatedIlluminator(settings);
            default:
                throw new RuntimeException("Programming Error: Illumator type " + settings.illuminatorType + " is not supported.");
        }
    }*/
    
    public static Illuminator getAutomaticInstance(IlluminatorSettings settings) {
        Function<String, IlluminatorSettings> generator = (devName) -> {
            IlluminatorSettings sets = (IlluminatorSettings) settings.copy();
            sets.name = devName;
            return sets;
        };
        
        Device.AutoFinder<Illuminator, IlluminatorSettings> finder = 
                new Device.AutoFinder<>(
                    IlluminatorSettings.class, 
                    generator,
                    XCite120LED.class,
                    SimulatedIlluminator.class,
                    DefaultIlluminator.class
                );
                
        Illuminator illum = finder.getAutoInstance(settings.name);
        if (illum == null) {
            Globals.mm().logs().logMessage("Autofinder No illuminator was found.");
        }
        return illum;
    }
    
    public enum Types {
        XCite120LED,
        Simulated,
    }
}
