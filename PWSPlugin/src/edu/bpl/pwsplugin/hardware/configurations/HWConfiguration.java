package edu.bpl.pwsplugin.hardware.configurations;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.micromanager.events.ConfigGroupChangedEvent;


public class HWConfiguration {
    HWConfigurationSettings settings;
    Map<String, ImagingConfiguration> imConfigs;
    private ImagingConfiguration activeConf_;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    
    public HWConfiguration(HWConfigurationSettings settings) throws MMDeviceException {
        this.settings = settings;
        imConfigs = new HashMap<>();
        for (int i=0; i < settings.configs.size(); i++) {
            ImagingConfigurationSettings s = settings.configs.get(i);
            imConfigs.put(s.name, ImagingConfiguration.getInstance(s));
        }
        if (imConfigs.size() > 0) {
            ImagingConfiguration conf = Iterables.get(imConfigs.values(), 0);
            this.activeConf_ = conf;
            this.activateImagingConfiguration(conf);//We must always have one, and only one, active configuration
        }
        Globals.mm().events().registerForEvents(this);
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
    
    public final void activateImagingConfiguration(ImagingConfiguration conf) throws MMDeviceException{
        this.activeConf_.deactivateConfiguration();
        conf.activateConfiguration();
        this.activeConf_ = conf;
    }
    
    public void addObjectiveChangedListener(PropertyChangeListener list) {
        pcs.addPropertyChangeListener("objective", list);
    }
    
    @Subscribe
    public void onConfigGroupChanged(ConfigGroupChangedEvent evt) {
        if (evt.getGroupName().equals(settings.objectiveConfigurationGroupName)) {
            pcs.firePropertyChange("objective", null, evt.getNewConfig());
        }
    }
    
    public List<String> validate() {
        //Check all imaging configurations for any errors
        List<String> errs = new ArrayList<>();
        for (ImagingConfiguration conf : this.imConfigs.values()) {
            errs.addAll(conf.validate());
        }
        return errs;
    }
    
    public void dispose() {
        //This removes references from external objects so that the object is deleted when it's references or lost
        Globals.mm().events().unregisterForEvents(this);
    }
}
