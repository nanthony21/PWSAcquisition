/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ImagingConfigurationSettings extends JsonableParam {
    public String name;
    public ImagingConfiguration.Types configType;
    public CamSettings camSettings;
    public TunableFilterSettings filtSettings;
}
