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

package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.beans.PropertyChangeListener;
import java.util.function.Function;
import mmcorej.DeviceType;


/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public abstract class TranslationStage1d implements Device {

   protected final TranslationStage1dSettings settings;

   public TranslationStage1d(TranslationStage1dSettings settings) {
      this.settings = settings;
   }

   public abstract double getPosUm() throws MMDeviceException;

   public abstract void setPosUm(double um) throws MMDeviceException, InterruptedException;

   public abstract void setPosRelativeUm(double um) throws MMDeviceException, InterruptedException;

   public abstract boolean supportsEscape();
   public abstract void setEscaped(boolean escape) throws MMDeviceException;
   public abstract boolean isEscaped();

   public abstract boolean hasAutoFocus();

   //The following only need to be implemented if `hasAutoFocus` is true

   public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
      throw new UnsupportedOperationException();
   }

   public boolean getAutoFocusEnabled() throws MMDeviceException {
      throw new UnsupportedOperationException();
   }

   public boolean getAutoFocusLocked() throws MMDeviceException {
      throw new UnsupportedOperationException();
   }

   /**
    * Search for a zStage position where the continuous focus can be locked.
    * Returns the position (microns) where lock is achievable. Throws an exception
    * if no lock is possible.
    * @return
    * @throws MMDeviceException
    */
   public double runFullFocus() throws MMDeviceException {
      throw new UnsupportedOperationException();
   }

   public enum Types {
      NikonTI,
      Simulated,
      NikonTI2,
      PriorProscan3
   }

   public static TranslationStage1d getAutomaticInstance() {

      Function<String, TranslationStage1dSettings> generator = (devName) -> {
         TranslationStage1dSettings settings = new TranslationStage1dSettings();
         settings.deviceName = devName;
         return settings;
      };

      Device.AutoFinder<TranslationStage1d, TranslationStage1dSettings> finder =
            new Device.AutoFinder<>(
                  TranslationStage1dSettings.class,
                  generator,
                  NikonTI2_zStage.class,
                  NikonTI_zStage.class,
                  SimulationStage1d.class,
                  PriorProscan3.class
            );

      return finder.scanAllDevices(DeviceType.StageDevice);
   }
}

