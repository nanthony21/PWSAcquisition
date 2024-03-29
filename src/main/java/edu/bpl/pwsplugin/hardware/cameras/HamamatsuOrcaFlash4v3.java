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
import java.util.ArrayList;
import java.util.List;

/**
 * @author N2-LiveCell
 */
public class HamamatsuOrcaFlash4v3 extends DefaultCamera {

   String _devName;

   public HamamatsuOrcaFlash4v3(CamSettings settings)  {
      super(settings);
      _devName = settings.name;
   }

   @Override
   public void initialize() throws MMDeviceException {
      super.initialize();
      try {
         Globals.core().setProperty(this._devName, "TRIGGER SOURCE",
               "SOFTWARE"); //This gives by far the best performance when acquiring PWS in non-TTL triggered mode.
         Globals.core().setProperty(this._devName, "MASTER PULSE TRIGGER SOURCE", "SOFTWARE");
         Globals.core().setProperty(this._devName, "MASTER PULSE MODE", "CONTINUOUS");
         Globals.core().setProperty(this._devName, "OUTPUT TRIGGER SOURCE[0]", "READOUT END");
         Globals.core().setProperty(this._devName, "OUTPUT TRIGGER POLARITY[0]", "POSITIVE");
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean supportsExternalTriggering() {
      return true;
   }

   @Override
   public boolean supportsTriggerOutput() {
      return true;
   }

   @Override
   public void configureTriggerOutput(boolean enable) throws MMDeviceException {
      try {
         if (enable) {
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER SOURCE[0]", "READOUT END");
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER POLARITY[0]", "POSITIVE");
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER KIND[0]", "PROGRAMABLE");
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER PERIOD[0]",
                  0.001); //The default is shorter than this and it is often missed by other devices.
         } else {
            Globals.core().setProperty(_devName, "OUTPUT TRIGGER KIND[0]", "LOW");
         }
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public void startSequence(int numImages, double delayMs, boolean externalTriggering)
         throws MMDeviceException {
      try {
         if (externalTriggering) {
            Globals.core().setProperty(this._devName, "TRIGGER SOURCE", "EXTERNAL");
            Globals.core().setProperty(this._devName, "TRIGGER DELAY",
                  delayMs / 1000); //This is in units of seconds.
         } else {
            double exposurems = this.getExposure();
            double readoutms =
                  12; //This is based on the frame rate calculation portion of the 13440-20CU camera. 9.7 us per line, reading two lines at once, 2048 lines -> 0.097*2048/2 ~= 10 ms. However testing has shown if we set this exactly then we end up missing every other frame and getting half our frame rate add a buffer of 2ms to be safe.
            double intervalMs = (exposurems + readoutms + delayMs);
            Globals.core().setProperty(this._devName, "TRIGGER SOURCE",
                  "MASTER PULSE"); //Make sure that Master Pulse is triggering the camera.
            Globals.core().setProperty(this._devName, "MASTER PULSE INTERVAL",
                  intervalMs / 1000.0); //In units of seconds
         }
         Globals.core().startSequenceAcquisition(numImages, 0,
               false); //The hamamatsu adapter throws an error if the interval is not 0. For some reason using the method signature that takes a device name results in a silence error here.
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public void stopSequence() throws MMDeviceException {
      try {
         Globals.core().stopSequenceAcquisition(
               this._devName); //This can hang for a long time when there is an error.
         Globals.core().setProperty(this._devName, "TRIGGER SOURCE",
               "SOFTWARE"); //Set the trigger source back ot what it was originally
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean identify() {
      try {
         return ((Globals.core().getDeviceName(this.settings.name).equals("HamamatsuHam_DCAM"))
               &&
               (Globals.core().getProperty(this.settings.name, "CameraName").equals("C13440-20C")));
      } catch (Exception e) {
         return false;
      }
   }

   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      try {
         if (!identify()) {
            errs.add(_devName + " is not a HamamatsuHam_DCAM device");
         }
      } catch (Exception e) {
         errs.add(e.getMessage());
      }
      return errs;
   }
}
