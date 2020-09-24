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
public class FluorSettings extends JsonableParam {
    public String imConfigName = ""; //The name of the "Imaging Configuration" used.
    public double exposure = 1000; //The exposure time of the camera in milliseconds.
    public String filterConfigName = ""; //The value that the filter "configuration group" should be set to in order to configure the fluorescence filter.
    public int tfWavelength = 550; //The wavelength that the tunable filter (if there is a tunable filter) should be set to in nanometers.
    public double focusOffset = 0; //The distance (in microns) that focus should be adjusted before taking an image.
}
