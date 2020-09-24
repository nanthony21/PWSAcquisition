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

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class SimulatedIlluminator extends DefaultIlluminator {
    
    public SimulatedIlluminator(IlluminatorSettings settings) throws Device.IDException {
        super(settings);
    }
    
    @Override
    public boolean identify() {
        try {
            return Globals.core().getDeviceName(this.settings.name).equals("DShutter");
        } catch (Exception e) {
            return false;
        } 
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!identify()) {
                errs.add(this.settings.name + " is not a simulated DemoShutter device");
            }

        } catch (Exception e) {
            errs.add(e.getMessage());
        }
        return errs;
    }
}