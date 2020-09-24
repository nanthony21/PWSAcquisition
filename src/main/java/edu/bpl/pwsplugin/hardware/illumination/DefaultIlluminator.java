/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DeviceType;

/**
 *
 * @author nick
 */
public class DefaultIlluminator implements Illuminator {
    //This can be any micromanager shutter device.
    //Classes can inherit from this to immplement more specific validation or other functionality.
    protected final IlluminatorSettings settings;
    
    public DefaultIlluminator(IlluminatorSettings settings) throws Device.IDException {
        this.settings = settings;
        if (!this.identify()) {
            throw new Device.IDException(String.format("Failed to identify class %s for device name %s", this.getClass().toString(), settings.name));
        }
    }
    
    @Override
    public void setShutter(boolean on) throws MMDeviceException {
        try {
            Globals.core().setShutterOpen(this.settings.name, on);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean identify() {
        try {
            return Globals.core().getDeviceType(this.settings.name).equals(DeviceType.ShutterDevice);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
        return new ArrayList<>();
    }

    @Override
    public void initialize() {}//Not sure what to do here
    
    @Override
    public void activate() {}//Not sure what to do here  
}
