/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nick
 */
public class XCite120LED extends DefaultIlluminator {
    
    public XCite120LED(IlluminatorSettings settings) throws Device.IDException {
        super(settings);
    }
    
    @Override
    public boolean identify() {
        try {
            return Globals.core().getDeviceName(this.settings.name).equals("XCite-Exacte");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!identify()) {
                errs.add(this.settings.name + " is not a XCite-exacte device");
            }
            
            //Make sure the lamp is warmed up.
            String lampTimeProp = "Lamp-On Time (s)";
            double lampOnTime = Double.valueOf(Globals.core().getProperty(this.settings.name, lampTimeProp));
            if (lampOnTime < 600) { //less than ten minutes
                errs.add(String.format("LED has only been allowed %.2f minutes (10 is recommended) to warm up", lampOnTime/60));
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }
        return errs;
    }
}