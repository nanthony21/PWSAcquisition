/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
