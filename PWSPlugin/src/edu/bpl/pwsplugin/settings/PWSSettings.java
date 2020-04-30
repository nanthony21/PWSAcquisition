/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;

//Make sure that everything here that extends jsonableparam gets registered on startup in the plugin class.

public class PWSSettings extends JsonableParam {

    public String imConfigName;
    public int wvStart;
    public int wvStop;
    public int wvStep;
    public double exposure;
    public boolean ttlTriggering;
    public boolean externalCamTriggering;

    public int[] getWavelengthArray() {
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
