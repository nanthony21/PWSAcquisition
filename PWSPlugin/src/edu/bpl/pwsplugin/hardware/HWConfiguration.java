package edu.bpl.pwsplugin.hardware;

import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


public class HWConfiguration {
    HWConfigurationSettings settings;
    Map<String, ImagingConfiguration> imConfigs;
    
    public HWConfiguration(HWConfigurationSettings settings) {
        this.settings = settings;
        imConfigs = new HashMap<>();
        for (int i=0; i < settings.configs.size(); i++) {
            ImagingConfigurationSettings s = settings.configs.get(i);
            imConfigs.put(s.name, ImagingConfiguration.getInstance(s));
        }
    }
    
    public HWConfigurationSettings getSettings() {
        return this.settings;
    }
    
    public ImagingConfiguration getImagingConfigurationByName(String name) {
        ImagingConfiguration conf = this.imConfigs.get(name);
        if (conf==null) {
            throw new NoSuchElementException("Could not find Imaging Configuration by the name " + name);
        }
        return conf;
    }
    
    public List<ImagingConfiguration> getImagingConfigurations() {
        return new ArrayList<>(this.imConfigs.values());
    }
    
    /*public void initialize() throws MMDeviceException {
        for (ImagingConfiguration conf : this.imConfigs.values()) {
            conf.initialize();
        }
    }
    
    public boolean hasBeenInitialized() {
        for (ImagingConfiguration conf : this.imConfigs.values()) {
            if (!conf.hasBeenInitialized()) {
                return false;
            }
        }
        return true; //We only get here if all imaging configurations are initialized.
    }*/
}
