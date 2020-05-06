package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.settings.CamSettings;
import java.util.ArrayList;
import java.util.List;
import org.micromanager.data.Image;

public class HamamatsuEMCCD extends Camera {
    CamSettings _settings;
    
    public HamamatsuEMCCD(CamSettings settings) {
        _settings = settings;
    }
    
    @Override
    public void initialize() {
        //Not sure if we need to do anything here.
    }
    
    @Override
    public boolean supportsExternalTriggering() { return false; }
        
    @Override
    public boolean supportsTriggerOutput() { return false; }
    
    @Override
    public void configureTriggerOutput(boolean enable) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The Hamamatsu EMCCD does not support output triggering");
    }
    
    @Override
    public String getName() { return this._settings.name; }
    
    @Override
    public void startSequence(int numImages, double delayMs, boolean externalTriggering) throws UnsupportedOperationException {
                throw new UnsupportedOperationException("The Hamamatsu EMCCD does not support output triggering");
    }
    
    @Override
    public void stopSequence() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The Hamamatsu EMCCD does not support output triggering");
    }
    
    @Override
    public void setExposure(double exposureMs) throws Exception {
        Globals.core().setExposure(this._settings.name, exposureMs);
    }
    
    @Override
    public double getExposure() throws Exception {
        return Globals.core().getExposure(this._settings.name);
    }
    
    @Override
    public Image snapImage() throws Exception {
        //TODO what if we are not set as the core camera at this point.
        //TODO if (Globals.core().getCameraDevice()!=_devName) {Globals.core().setCameraDevice(_devName);}
        Globals.core().snapImage();
        return Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
    }
    
    @Override
    public CamSettings getSettings() {
        return _settings;
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!Globals.core().getDeviceName(this._settings.name).equals("HamamatsuHam_DCAM")) {
                errs.add(this._settings.name + " is not a HamamatsuHam_DCAM device");
            }

            //Check the CCD temperature is ok.
            String tempProp = "CCDTemperature";
            String spProp = "Temperature Set Point";
            double temp = Double.valueOf(Globals.core().getProperty(this._settings.name, tempProp));
            double tempset = Double.valueOf(Globals.core().getProperty(this._settings.name, spProp));
            if (temp-tempset > 0.1) {
                errs.add("Camera temperature is " + String.valueOf(temp-tempset) + "(>0.1C) off from setpoint.");
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }

        return errs;
    }
    
}
