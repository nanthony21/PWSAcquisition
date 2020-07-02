package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;


public interface Illuminator extends Device {

    public void setShutter(boolean on) throws MMDeviceException;
    
    public static Illuminator getInstance(IlluminatorSettings settings) {
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
    }
    
    public enum Types {
        XCite120LED,
        Simulated,
    }
}
