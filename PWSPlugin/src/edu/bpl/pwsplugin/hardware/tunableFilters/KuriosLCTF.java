/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;

/**
 *
 * @author N2-LiveCell
 */
public class KuriosLCTF extends TunableFilter {
    String devName = "kuriosLCTF";
    
    public KuriosLCTF() {
    }
    
    @Override
    public void setWavelength(int wavelength) throws Exception {
        Globals.core().setProperty(devName, "Wavelength", String.valueOf(wavelength));
    }
    
    @Override
    public int getWavelength() throws Exception{ 
        int wv = Integer.valueOf(Globals.core().getProperty(devName, "Wavelength"));
        return wv;
    }
    
    @Override
    public boolean supportsSequencing() { return true; } 
}
