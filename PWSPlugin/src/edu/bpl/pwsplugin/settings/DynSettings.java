/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class DynSettings extends JsonableParam {
    //Settings for a single Dynamics acquisition.
    
    public String imConfigName = ""; //The name of the "Imaging Configuration" used.
    public double exposure = 50; //The exposure time of the camera in milliseconds.
    public int wavelength = 550; //The wavelength that the tunable filter should be set to in nanometers.
    public int numFrames = 200; //The number of images to take.
    
}
