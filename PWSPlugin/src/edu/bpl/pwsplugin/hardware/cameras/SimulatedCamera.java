/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.util.ArrayList;
import java.util.List;
import org.micromanager.data.Image;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class SimulatedCamera extends Camera {
    private final CamSettings _settings;
    
    public SimulatedCamera(CamSettings settings) {
        _settings = settings;
    }
    
    @Override
    public void activate() throws MMDeviceException {
        try {
            Globals.core().setCameraDevice(_settings.name);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public void initialize() {}
    
    @Override
    public boolean supportsExternalTriggering() { return false; }
    
    @Override
    public boolean supportsTriggerOutput() { return false; }
    
    @Override
    public void configureTriggerOutput(boolean enable) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getName() { //Get the device name used in Micro-Manager.
        return _settings.name;
    }
    
    @Override
    public void startSequence(int numImages, double delayMs, boolean externalTriggering) throws MMDeviceException{
        if (externalTriggering) {
            throw new UnsupportedOperationException();
        } else {
            try {
                Globals.core().startSequenceAcquisition(_settings.name, numImages, delayMs, false);
            } catch (Exception e) {
               throw new MMDeviceException(e);
            }
        }
    }
    
    @Override
    public void stopSequence() throws MMDeviceException {
        try {
            Globals.core().stopSequenceAcquisition(_settings.name);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public CamSettings getSettings() {
        return _settings;
    }
    
    @Override
    public void setExposure(double exposureMs) throws MMDeviceException {
        try {
            Globals.core().setExposure(_settings.name, exposureMs);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public double getExposure() throws MMDeviceException {
        try {
            return Globals.core().getExposure(_settings.name);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public Image snapImage() throws MMDeviceException {
        try {
            Globals.core().snapImage();
            return Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!Globals.core().getDeviceName(_settings.name).equals("DCam")) {
                errs.add(_settings.name + " is not a simulated DemoCamera device");
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }
        return errs;
    }
}
