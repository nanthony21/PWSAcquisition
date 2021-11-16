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
 * Metadata for a PWS measurement.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class PWSMetadata extends MetadataBase {

   private final List<Double> wavelengths; //The wavelengths associated with each image in the image cube.
   private final Double exposure; //The exposure time that was used. milliseconds.

   public PWSMetadata(MetadataBase base, List<Double> wavelengths, Double exposure) {
      super(base);
      this.wavelengths = wavelengths;
      this.exposure = exposure;
   }

   @Override
   public JsonObject toJson() {
      JsonObject md = super.toJson();
      md.add("wavelengths", GsonUtils.getGson().toJsonTree(wavelengths).getAsJsonArray());
      md.addProperty("exposure", exposure);
      return md;
   }

}
