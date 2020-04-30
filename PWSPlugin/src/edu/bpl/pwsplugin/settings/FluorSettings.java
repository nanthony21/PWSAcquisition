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
    
    public String imConfigName;
    public double exposure;
    public String filterConfigName;
    public int tfWavelength;
    
}
