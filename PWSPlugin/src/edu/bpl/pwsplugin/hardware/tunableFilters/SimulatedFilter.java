/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.settings.TunableFilterSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class SimulatedFilter extends TunableFilter {
    private TunableFilterSettings settings;
    private int wv;
    
    public SimulatedFilter(TunableFilterSettings settings) {
        this.settings = settings;
    }
    
    public void setWavelength(int wavelength) throws Exception {
        wv = wavelength;
    }
    
    public int getWavelength() throws Exception {
        return wv;
    }
    
    public boolean supportsSequencing() {
        return false;
    }
    
    public int getMaxSequenceLength() throws Exception {
        throw new UnsupportedOperationException();
    }
    
    public void loadSequence(int[] wavelengthSequence) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    public void startSequence() throws Exception {
        throw new UnsupportedOperationException();
    }
    
    public void stopSequence() throws Exception {
        throw new UnsupportedOperationException();
    }
    
    public boolean isBusy() throws Exception {
        return false;
    }
    
    public double getDelayMs() throws Exception {
        return 50;
    }
    
    public TunableFilterSettings getSettings() { return settings; }
    
    public List<String> validate() {
         return new ArrayList<String>();
    }
}
