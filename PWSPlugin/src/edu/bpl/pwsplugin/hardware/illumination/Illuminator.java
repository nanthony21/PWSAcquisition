package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.List;


public abstract class Illuminator {
    public abstract List<String> validate(); //Return a list of strings for every error detected in the configuration. return empty list if no errors found.

    public abstract void initialize() throws MMDeviceException; // One time initialization of device
    
    public abstract void activate() throws MMDeviceException; //Make sure this device is ready for usage, may be run many times.
    
    public abstract void setShutter(boolean on) throws MMDeviceException;
    
    public static Illuminator getInstance(IlluminatorSettings settings) {
        if (settings.illuminatorType == Types.XCite120LED) {
            return new XCite120LED(settings);
        } else if (settings.illuminatorType == Types.Simulated) {
            return new SimulatedIlluminator(settings);
        } else {
            throw new RuntimeException("Programming Error: Illumator type " + settings.illuminatorType + " is not supported.");
        }
    }
    
    public enum Types {
        XCite120LED,
        Simulated,
    }
}
