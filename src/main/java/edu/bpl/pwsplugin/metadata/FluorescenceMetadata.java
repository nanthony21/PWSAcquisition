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
package edu.bpl.pwsplugin.metadata;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class FluorescenceMetadata extends MetadataBase {

   private final String filterBlock;
   private final Double exposure;
   private final Integer wavelength;

   public FluorescenceMetadata(MetadataBase base, String filterBlock, Double exposure,
         Integer wavelength) {
      super(base);
      this.filterBlock = filterBlock;
      this.exposure = exposure;
      this.wavelength = wavelength;
   }

   @Override
   public JsonObject toJson() {
      JsonObject md = super.toJson();
      md.addProperty("filterBlock", filterBlock);
      md.addProperty("exposure", exposure);
      if (wavelength == null) {
         md.add("wavelength", JsonNull.INSTANCE);
      } else {
         md.addProperty("wavelength", wavelength);
      }
      return md;
   }
}
