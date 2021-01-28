///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.util.function.Function;
import org.micromanager.data.Image;

/**
 *
 * @author nicke
 */
public interface Camera extends Device{

    void configureTriggerOutput(boolean enable) throws MMDeviceException; //Turn transmission of TTL pulses on or off.

    double getExposure() throws MMDeviceException;

    String getName(); //Get the device name used in Micro-Manager.

    CamSettings getSettings();

    void setExposure(double exposureMs) throws MMDeviceException;

    Image snapImage() throws MMDeviceException;

    void startSequence(int numImages, double delayMs, boolean externalTriggering) throws MMDeviceException; //If the camera support "Trigger output" then this should start the seqeunce

    void stopSequence() throws MMDeviceException; // Clean up and reset the sequence. Only needed for cameras that support trigger output.

    boolean supportsExternalTriggering(); //True if the camera can have new image acquisitions triggered by an incoming TTL signal

    //public abstract void configureExternalTriggering(boolean enable, double triggerDelayMs) throws MMDeviceException; //Turn external triggering on or off.
    boolean supportsTriggerOutput(); //True if the camera can send a TTL trigger at the end of each new image it acquires.

    /*public static Camera getInstance(CamSettings settings) {
        if (null == settings.camType) {
            throw new NullPointerException("This shouldn't ever happen"); //This shouldn't ever happen.
        } else switch (settings.camType) {
            case HamamatsuOrca4V3:
                return new HamamatsuOrcaFlash4v3(settings);
            case HamamatsuEMCCD:
                return new HamamatsuEMCCD(settings);
            case Simulated:
                return new SimulatedCamera(settings);
            case HamamatsuOrcaFlash2_8:
                return new HamamatsuOrcaFlash2_8(settings);
            default:
                return null; //This shouldn't ever happen.
        }
    }*/
    
    public static Camera getAutomaticInstance(CamSettings settings) {
        Function<String, CamSettings> generator = (devName) -> {
            CamSettings sets = (CamSettings) settings.copy();
            sets.name = devName;
            return sets;
        };
        
        Device.AutoFinder<Camera, CamSettings> finder = 
                new Device.AutoFinder<>(
                    CamSettings.class, 
                    generator,
                    HamamatsuEMCCD.class,
                    HamamatsuOrcaFlash2_8.class,
                    HamamatsuOrcaFlash4v3.class,
                    HamamatsuOrcaFlash4v1.class,
                    SimulatedCamera.class
                );
                
        return finder.getAutoInstance(settings.name);
    }
    
    public enum Types {
        HamamatsuOrca4V3,
        HamamatsuEMCCD,
        Simulated,
        HamamatsuOrcaFlash2_8,
        HamamatsuOrcaFlash4v1;
    }
    
}
