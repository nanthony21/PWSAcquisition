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

package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.List;

/**
 * These settings describe the hardware configuration of the plugin. It may contain multiple "Imaging Configurations"
 * The program only can use a single hardware configuration.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class HWConfigurationSettings extends JsonableParam {
   public String systemName = "";
         //The name of this microscope system. This will be saved in metadata.
   public List<ImagingConfigurationSettings> configs = new ArrayList<>();
         //A list of settings for various "Imaging Configurations"

   public ImagingConfigurationSettings getConfigurationByName(String name) {
      for (int i = 0; i < this.configs.size(); i++) {
         if (this.configs.get(i).name.equals(name)) {
            return this.configs.get(i);
         }
      }
      return null;
   }
}
