/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.settings;

import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
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
    public String fluorescenceConfigGroup = ""; //The name of the configuration group used to control fluorescence filter.
    public static final String MANUALFLUORESCENCENAME = "MANUAL CONTROL"; // If fluorescenceConfigGroup is equal to this then the software will to prompt the user to manually set the filter.
}
