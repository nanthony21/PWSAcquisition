package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.settings.IlluminatorSettings;
import java.util.List;


public abstract class Illuminator {
    public abstract List<String> validate(); //Return a list of strings for every error detected in the configuration. return empty list if no errors found.

    public abstract void initialize() throws Exception;
    
    public abstract void setShutter(boolean on) throws Exception;
    
    public static Illuminator getInstance(IlluminatorSettings settings) {
        if (settings.illuminatorType == Types.XCite120LED) {
            return new XCite120LED(settings);
        } else {
            throw new RuntimeException("Programming Error: Illumator type " + settings.illuminatorType + " is not supported.");
        }
    }
    
    public enum Types {
        XCite120LED,
    }
}
