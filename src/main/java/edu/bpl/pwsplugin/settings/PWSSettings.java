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

//Make sure that everything here that extends jsonableparam gets registered on startup in the plugin class.

public class PWSSettings extends JsonableParam {
    /* These settings describe a single acquisition of PWS.
    
    */
    public String imConfigName = ""; //The "Imaging Configuration" to be used.
    public int wvStart = 500; //The wavelengh (nm) to start scanning at.
    public int wvStop = 700; //the wavelength (nm) to stop scanning at.
    public int wvStep = 2; //The interval of wavelengths (nm) to scan in.
    public double exposure = 100; //The exposure time of the camera in milliseconds.
    public boolean ttlTriggering = false; //Whether to attempt acquiring with the camera triggering a sequence of wavelengths via a TTL cable. Not all hardware supports this but it is much faster.
    public boolean externalCamTriggering = false; //Whether the tunable filter will use a TTL cable to trigger the camera when it is done tuning. `ttlTriggering` must also be true for this to be true.

    public int[] getWavelengthArray() { //Generate the array of wavelengths used based on the start, stop, and step settings.
        int numWvs = java.lang.Math.abs(wvStart - wvStop) / wvStep + 1;
        int[] wvs = new int[numWvs];
        int index = 0;
        for (int i = wvStart; i <= wvStop; i += wvStep) {
            wvs[index] = i;
            index++;
        }
        return wvs;
    }
    
}
