/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class HWConfigurationSettings extends JsonableParam {
    /*
    These settings describe the hardware configuration of the plugin. It may contain multiple "Imaging Configurations"
    The program only can use a single hardware configuration.
    */
    
    public String systemName = ""; //The name of this microscope system. This will be saved in metadata.
    public String objectiveConfigurationGroupName = ""; //The name of the Micro-Manager "configuration group" that adjusts which objective is used.
    public List<ImagingConfigurationSettings> configs = new ArrayList<>(); //A list of settings for various "Imaging Configurations"

    public ImagingConfigurationSettings getConfigurationByName(String name) {
        for (int i = 0; i < this.configs.size(); i++) {
            if (this.configs.get(i).name.equals(name)) {
                return this.configs.get(i);
            }
        }
        return null;
    }
}
