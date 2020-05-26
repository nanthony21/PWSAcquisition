/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.settings.CamSettings;
import java.util.ArrayList;
import java.util.List;
import org.micromanager.data.Image;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class SimulatedCamera extends Camera {
    CamSettings _settings;
    String _devName;
    
    public SimulatedCamera(CamSettings settings) {
        _settings = settings;
        _devName = settings.name;
    }
    
    @Override
    public void initialize() throws Exception {}
    
    @Override
    public boolean supportsExternalTriggering() { return false; }
    
    @Override
    public boolean supportsTriggerOutput() { return false; }
    
    @Override
    public void configureTriggerOutput(boolean enable) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getName() { //Get the device name used in Micro-Manager.
        return this._devName;
    }
    
    @Override
    public void startSequence(int numImages, double delayMs, boolean externalTriggering) throws Exception{
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void stopSequence() throws Exception {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CamSettings getSettings() {
        return _settings;
    }
    
    @Override
    public void setExposure(double exposureMs) throws Exception {
        Globals.core().setExposure(_devName, exposureMs);
    }
    
    @Override
    public double getExposure() throws Exception {
        return Globals.core().getExposure(_devName);
    }
    
    @Override
    public Image snapImage() throws Exception {
        Globals.core().snapImage();
        return Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!Globals.core().getDeviceName(this._devName).equals("DCam")) {
                errs.add(_devName + " is not a simulated DemoCamera device");
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }
        return errs;
    }
}
