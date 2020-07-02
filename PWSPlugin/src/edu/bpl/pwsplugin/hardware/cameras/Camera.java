/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.util.List;
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

    public static Camera getInstance(CamSettings settings) {
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
    }
    
    public enum Types {
        HamamatsuOrca4V3,
        HamamatsuEMCCD,
        Simulated,
        HamamatsuOrcaFlash2_8;
    }
    
}
