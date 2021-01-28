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
package edu.bpl.pwsplugin.hardware.cameras;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nicke
 */
public class HamamatsuOrcaFlash2_8 extends DefaultCamera { 
    
    public HamamatsuOrcaFlash2_8(CamSettings settings) throws Device.IDException {
        super(settings);
    }

    @Override
    public boolean supportsExternalTriggering() { return false; }
        
    @Override
    public boolean supportsTriggerOutput() { return false; } //The camera actually does support this but we don't have the cable for it and have never tried.
    
    @Override
    public void configureTriggerOutput(boolean enable) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The Flash 2.8 does not support output triggering");
    }
    
    @Override
    public boolean identify() {
        try {
            return ((Globals.core().getDeviceName(this.settings.name).equals("HamamatsuHam_DCAM"))
                && 
                (Globals.core().getProperty(this.settings.name, "CameraName").equals("C11440-10C")));
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
        } catch (Exception e) {
            errs.add(e.getMessage());
        }

        return errs;
    }
    
}
