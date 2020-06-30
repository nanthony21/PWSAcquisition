package edu.bpl.pwsplugin.hardware.configurations;

import com.google.common.collect.Iterables;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
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
    private ImagingConfiguration activeConf_;
    
    public HWConfiguration(HWConfigurationSettings settings) {
        this.settings = settings;
        imConfigs = new HashMap<>();
        for (int i=0; i < settings.configs.size(); i++) {
            ImagingConfigurationSettings s = settings.configs.get(i);
            imConfigs.put(s.name, ImagingConfiguration.getInstance(s));
        }
        if (imConfigs.size() > 0) {
            ImagingConfiguration conf = Iterables.get(imConfigs.values(), 0);
            this.activeConf_ = conf;
            try {
                this.activateImagingConfiguration(conf);//We must always have one, and only one, active configuration
            } catch (MMDeviceException e) {
                throw new RuntimeException(e); //This is messy, hopefully it just never comes up.
            }
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
    
    public ImagingConfiguration getActiveConfiguration() {
        return activeConf_;
    }
    
    public void activateImagingConfiguration(ImagingConfiguration conf) throws MMDeviceException{
        this.activeConf_.deactivateConfiguration();
        conf.activateConfiguration();
        this.activeConf_ = conf;
    }
}