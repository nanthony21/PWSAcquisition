package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.hardware.cameras.HamamatsuOrcaFlash4v3;
import edu.bpl.pwsplugin.settings.CamSettings;
import java.util.List;
import org.micromanager.data.Image;


public abstract class Illuminator {
    public abstract List<String> validate(); //Return a list of strings for every error detected in the configuration. return empty list if no errors found.

    public abstract void initialize() throws Exception;
    
    public static Illuminator getInstance(IlluminatorSettings settings) {
        if (settings.type == Types.XCite120LED) {
            return new XCite120LED(settings);
        } else {
            throw new RuntimeException("Programming Error: Illumator type " + settings.type + " is not supported.");
        }
    }
    
    public enum Types {
        XCite120LED,
    }
}
