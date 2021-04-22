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
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class DynSettings extends JsonableParam {
   //Settings for a single Dynamics acquisition.

   public String imConfigName = ""; //The name of the "Imaging Configuration" used.
   public double exposure = 50; //The exposure time of the camera in milliseconds.
   public int wavelength = 550; //The wavelength that the tunable filter should be set to in nanometers.
   public int numFrames = 200; //The number of images to take.

}
