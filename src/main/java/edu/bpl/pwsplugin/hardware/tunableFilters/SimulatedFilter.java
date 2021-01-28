///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
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
    public boolean identify() {
        try {
            return Globals.core().getDeviceLibrary(this.settings.name).equals("DCam"); //We don't have a great way to identify a simulation device, just make sure that the device comes from the `demo` library.
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
         return new ArrayList<>();
    }
    
    @Override
    public void initialize() {}//Not sure what to do here
    
    @Override
    public void activate() {}//Not sure what to do here
}
