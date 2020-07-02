package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.util.ArrayList;
import java.util.List;

public class HamamatsuEMCCD extends DefaultCamera {
    
    public HamamatsuEMCCD(CamSettings settings) {
        super(settings);
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
    public boolean identify() {
        try {
            return ((Globals.core().getDeviceName(this.settings.name).equals("HamamatsuHam_DCAM"))
                && 
                (Globals.core().getProperty(this.settings.name, "CameraName").equals("C9100-13")));
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!identify()) {
                errs.add(this.settings.name + " is not a HamamatsuHam_DCAM device");
            }

            //Check the CCD temperature is ok.
            String tempProp = "CCDTemperature";
            String spProp = "Temperature Set Point";
            double temp = Double.valueOf(Globals.core().getProperty(this.settings.name, tempProp));
            double tempset = Double.valueOf(Globals.core().getProperty(this.settings.name, spProp));
            if (temp-tempset > 0.1) {
                errs.add("Camera temperature is " + String.valueOf(temp-tempset) + "(>0.1C) off from setpoint.");
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }

        return errs;
    }
    
}
