/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.List;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class HWConfigurationSettings extends JsonableParam {
    
    public String systemName;
    public List<ImagingConfigurationSettings> configs;

    public ImagingConfigurationSettings getConfigurationByName(String name) {
        for (int i = 0; i < this.configs.size(); i++) {
            if (this.configs.get(i).name.equals(name)) {
                return this.configs.get(i);
            }
        }
        return null;
    }
    
}
