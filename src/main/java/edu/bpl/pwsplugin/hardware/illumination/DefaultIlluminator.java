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

package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DeviceType;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class DefaultIlluminator implements Illuminator {

   //This can be any micromanager shutter device.
   //Classes can inherit from this to implement more specific validation or other functionality.
   protected final IlluminatorSettings settings;

   public DefaultIlluminator(IlluminatorSettings settings) throws Device.IDException {
      this.settings = settings;
      if (!this.identify()) {
         throw new Device.IDException(
               String.format("Failed to identify class %s for device name %s",
                     this.getClass().toString(), settings.name));
      }
   }

   @Override
   public void setShutter(boolean on) throws MMDeviceException {
      try {
         Globals.core().setShutterOpen(this.settings.name, on);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean identify() {
      try {
         return Globals.core().getDeviceType(this.settings.name).equals(DeviceType.ShutterDevice);
      } catch (Exception e) {
         return false;
      }
   }

   @Override
   public List<String> validate() {
      return new ArrayList<>();
   }

   @Override
   public void initialize() {
   }//Not sure what to do here

   @Override
   public void activate() {
   }//Not sure what to do here
}
