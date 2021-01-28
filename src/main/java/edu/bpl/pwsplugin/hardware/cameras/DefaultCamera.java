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
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import java.util.List;
import mmcorej.MMCoreJ;
import org.micromanager.data.Image;

/**
 *
 * @author nicke
 */
public abstract class DefaultCamera implements Camera {
    protected CamSettings settings;
    
    public DefaultCamera(CamSettings settings) throws Device.IDException {
        this.settings = settings;
        if (!this.identify()) {
            throw new Device.IDException(String.format("Failed to identify class %s for device name %s", this.getClass().toString(), settings.name));
        }
    }

    @Override
    public void initialize() throws MMDeviceException {
        try {
            Globals.core().setProperty(settings.name, MMCoreJ.getG_Keyword_Binning(), settings.binning);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        Globals.mm().app().refreshGUI(); //update gui to reflect new binning.
    }

    @Override
    public void activate() throws MMDeviceException {
        try {
            Globals.core().setCameraDevice(settings.name);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        } 
    }

    @Override
    public String getName() {
        return settings.name;
    }


    @Override
    public void setExposure(double exposureMs) throws MMDeviceException {
        try {
            Globals.core().setExposure(settings.name, exposureMs);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }

    @Override
    public double getExposure() throws MMDeviceException {
        try {
            return Globals.core().getExposure(settings.name);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }

    @Override
    public Image snapImage() throws MMDeviceException {
        try {
            Globals.core().snapImage();
            return Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }

    @Override
    public CamSettings getSettings() {
        return settings;
    }
        
    @Override
    public void startSequence(int numImages, double delayMs, boolean externalTriggering) throws MMDeviceException{
        if (externalTriggering) {
            throw new UnsupportedOperationException();
        } else {
            try {
                Globals.core().startSequenceAcquisition(settings.name, numImages, delayMs, false);
            } catch (Exception e) {
               throw new MMDeviceException(e);
            }
        }
    }
    
    @Override
    public void stopSequence() throws MMDeviceException {
        try {
            Globals.core().stopSequenceAcquisition(settings.name);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    //Override these
    @Override
    public abstract boolean identify();
    
    @Override
    public abstract List<String> validate();

    
    @Override
    public abstract boolean supportsExternalTriggering();
    
    @Override
    public abstract boolean supportsTriggerOutput();

    @Override
    public abstract void configureTriggerOutput(boolean enable) throws MMDeviceException;
}
