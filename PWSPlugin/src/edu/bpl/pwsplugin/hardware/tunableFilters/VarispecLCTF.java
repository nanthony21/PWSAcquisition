/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class VarispecLCTF extends DefaultTunableFilter {
    String devName = "VarispecLCTF";
    
    public VarispecLCTF() {
        super("VarispecLCTF", "Wavelength");
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        List<String> devs = Arrays.asList(Globals.core().getLoadedDevices().toArray());
        if (!devs.contains(this.devName)) {
            errs.add("VarispecLCTF: Could not find device named " + this.devName + ".");
        }
        return errs; 
    }
}