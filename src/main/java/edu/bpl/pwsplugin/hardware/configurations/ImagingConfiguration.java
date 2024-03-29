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

package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import java.util.List;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public interface ImagingConfiguration {

   Camera camera();

   Illuminator illuminator();

   boolean hasTunableFilter();

   TunableFilter tunableFilter();

   List<String> validate() throws MMDeviceException;

   TranslationStage1d zStage();

   void activateConfiguration() throws MMDeviceException;

   void deactivateConfiguration() throws MMDeviceException;

   /**
    * The name of the configuration group used to control fluorescence filter. Return null if manual control is required.
    * @return
    */
   String getFluorescenceConfigGroup();

   static ImagingConfiguration getInstance(ImagingConfigurationSettings settings)
         throws MMDeviceException {
      if (null == settings.configType) {
         return null; //This shouldn't ever happen.
      } else {
         switch (settings.configType) {
            case SpectralCamera:
               return new SpectralCamera(settings);
            case StandardCamera:
               return new StandardCamera(settings);
            default:
               return null; //This shouldn't ever happen.
         }
      }
   }

   enum Types {
      SpectralCamera,
      StandardCamera;
   }

}
