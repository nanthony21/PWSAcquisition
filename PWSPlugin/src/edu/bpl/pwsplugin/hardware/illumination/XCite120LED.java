/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.settings.IlluminatorSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nick
 */
public class XCite120LED extends Illuminator {
    private IlluminatorSettings settings;
    
    public XCite120LED(IlluminatorSettings settings) {
        this.settings = settings;
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        try {
            if (!Globals.core().getDeviceName(this.settings.name).equals("XCite-exacte")) { //TODO check this name is right
                errs.add(this.settings.name + " is not a XCite-exacte device");
            }
            
            //Make sure the lamp is warmed up.
            String lampTimeProp = "Lamp-On Time (s)";
            double lampOnTime = Double.valueOf(Globals.core().getProperty(this.settings.name, lampTimeProp));
            if (lampOnTime < 600) { //less than ten minutes
                errs.add("LED has only been allowed " + String.valueOf(lampOnTime/60) + " minutes (10 is recommended) to warm up");
            }
        } catch (Exception e) {
            errs.add(e.getMessage());
        }
        return errs;
    }

    @Override
    public void initialize() {}//Not sure what to do here
    
}