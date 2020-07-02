/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class SimulatedFilter implements TunableFilter {
    private final TunableFilterSettings settings;
    private int wv;
    
    public SimulatedFilter(TunableFilterSettings settings) {
        this.settings = settings;
    }
    
    @Override
    public void setWavelength(int wavelength) {
        wv = wavelength;
    }
    
    @Override
    public int getWavelength() {
        return wv;
    }
    
    @Override
    public boolean supportsSequencing() {
        return false;
    }
    
    @Override
    public int getMaxSequenceLength() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void loadSequence(int[] wavelengthSequence) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void startSequence() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void stopSequence() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isBusy() {
        return false;
    }
    
    @Override
    public double getDelayMs() {
        return 50;
    }
    
    @Override
    public TunableFilterSettings getSettings() { return settings; }
    
    @Override
    public List<String> validate() {
         return new ArrayList<String>();
    }
    
    @Override
    public void initialize() {}//Not sure what to do here
    
    @Override
    public void activate() {}//Not sure what to do here
}
