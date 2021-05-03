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

package edu.bpl.pwsplugin.hardware.settings;

import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ImagingConfigurationSettings extends JsonableParam {

   public String name = "";
   public ImagingConfiguration.Types configType = ImagingConfiguration.Types.StandardCamera;
   public CamSettings camSettings = new CamSettings();
   public TunableFilterSettings filtSettings = new TunableFilterSettings();
   public IlluminatorSettings illuminatorSettings = new IlluminatorSettings();
   public String configurationGroup = "";
   public String configurationName = "";
   public String fluorescenceConfigGroup = "";
         //The name of the configuration group used to control fluorescence filter.
   public static final String MANUALFLUORESCENCENAME = "MANUAL CONTROL";
         // If fluorescenceConfigGroup is equal to this then the software will to prompt the user to manually set the filter.
}
