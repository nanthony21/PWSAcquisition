/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.micromanager.events.ConfigGroupChangedEvent;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class HWConfigurationSettings extends JsonableParam {
    
    public String systemName = "";
    public String objectiveConfigurationGroupName = "";
    public List<ImagingConfigurationSettings> configs = new ArrayList<>();

    public ImagingConfigurationSettings getConfigurationByName(String name) {
        for (int i = 0; i < this.configs.size(); i++) {
            if (this.configs.get(i).name.equals(name)) {
                return this.configs.get(i);
            }
        }
        return null;
    }
}
