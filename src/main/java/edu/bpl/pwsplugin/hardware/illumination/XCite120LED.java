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
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nick
 */
public class XCite120LED extends DefaultIlluminator {

   public XCite120LED(IlluminatorSettings settings) throws Device.IDException {
      super(settings);
   }

   @Override
   public boolean identify() {
      try {
         return Globals.core().getDeviceName(this.settings.name).equals("XCite-Exacte");
      } catch (Exception e) {
         return false;
      }
   }

   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      try {
         if (!identify()) {
            errs.add(this.settings.name + " is not a XCite-exacte device");
         }

         //Make sure the lamp is warmed up.
         String lampTimeProp = "Lamp-On Time (s)";
         double lampOnTime = Double
               .valueOf(Globals.core().getProperty(this.settings.name, lampTimeProp));
         if (lampOnTime < 600) { //less than ten minutes
            errs.add(String.format(
                  "LED has only been allowed %.2f minutes (10 is recommended) to warm up",
                  lampOnTime / 60));
         }
      } catch (Exception e) {
         errs.add(e.getMessage());
      }
      return errs;
   }
}