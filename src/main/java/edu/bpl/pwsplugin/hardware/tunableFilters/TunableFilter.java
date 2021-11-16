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

import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.function.Function;


/**
 * @author N2-LiveCell
 */
public interface TunableFilter extends Device {

   void setWavelength(int wavelength) throws MMDeviceException;

   int getWavelength() throws MMDeviceException;

   boolean supportsSequencing();

   int getMaxSequenceLength() throws MMDeviceException;

   void loadSequence(int[] wavelengthSequence) throws MMDeviceException;

   void startSequence() throws MMDeviceException;

   void stopSequence() throws MMDeviceException;

   boolean isBusy() throws MMDeviceException;

   double getDelayMs() throws MMDeviceException;

   TunableFilterSettings getSettings();


   static TunableFilter getAutomaticInstance(TunableFilterSettings settings) {
      Function<String, TunableFilterSettings> generator = (devName) -> {
         TunableFilterSettings sets = (TunableFilterSettings) settings.copy();
         sets.name = devName;
         return sets;
      };

      Device.AutoFinder<TunableFilter, TunableFilterSettings> finder =
            new Device.AutoFinder<>(
                  TunableFilterSettings.class,
                  generator,
                  Spectra3.class,
                  VarispecLCTF.class,
                  KuriosLCTF.class,
                  SimulatedFilter.class
            );

      return finder.getAutoInstance(settings.name);
   }

   enum Types {
      VARISPECLCTF,
      KURIOSLCTF,
      Simulated,
      SPECTRA3;
   }
}
