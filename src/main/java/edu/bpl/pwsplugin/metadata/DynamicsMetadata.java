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

import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.util.List;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class DynamicsMetadata extends MetadataBase {

   private final Double wavelength;
   private final List<Double> times;
   private final Double exposure;

   public DynamicsMetadata(MetadataBase base, Double wavelength, List<Double> times,
         Double exposure) {
      super(base);
      this.wavelength = wavelength;
      this.times = times;
      this.exposure = exposure;
   }

   @Override
   public JsonObject toJson() {
      JsonObject md = super.toJson();
      md.addProperty("wavelength", wavelength);
      md.addProperty("exposure", exposure);
      md.add("times", GsonUtils.getGson().toJsonTree(times).getAsJsonArray());
      return md;
   }
}
