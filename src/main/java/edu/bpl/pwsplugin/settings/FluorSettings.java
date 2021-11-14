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

import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class FluorSettings extends JsonableParam {

   public String imConfigName = ""; //The name of the "Imaging Configuration" used.
   public double exposure = 1000; //The exposure time of the camera in milliseconds.
   public String filterConfigName = "";
         //The value that the filter "configuration group" should be set to in order to configure the fluorescence filter.
   public int tfWavelength = 550;
         //The wavelength that the tunable filter (if there is a tunable filter) should be set to in nanometers.
   public double focusOffset = 0;
         //The distance (in microns) that focus should be adjusted before taking an image.
}
