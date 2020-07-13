
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.hardware.configurations.HWConfiguration;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.JOptionPane;
import mmcorej.CMMCore;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;

public class Globals {
    private static Globals instance = null;
    private Studio studio_ = null;
    private AcquisitionManager acqMan_;
    private HWConfiguration config;
    private PluginFrame frame;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private Globals() {}
    
    private static Globals instance() {
        //Stores a reference to the singleton instance of this class
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }
    
    public static void init(Studio studio) {
        instance().studio_ = studio;
        instance().acqMan_ = new AcquisitionManager();
        instance().frame = new PluginFrame();
        try {
            instance().config = new HWConfiguration(new HWConfigurationSettings()); //Set these even though they should be overridden when the settings are loaded.
        } catch (MMDeviceException e) {
            throw new RuntimeException(e); 
        }

        PWSPluginSettings settings = Globals.loadSettings();
        instance().frame.populateFields(settings);
        if (settings != null) {
            Globals.setHardwareConfigurationSettings(settings.hwConfiguration);
        }
    }
    
    public static void saveSettings(PWSPluginSettings settings) {
        Globals.mm().profile().getSettings(PWSPlugin.class).putString("settings", settings.toJsonString());
    }
    
    private static PWSPluginSettings loadSettings() {
        String settingsStr = Globals.mm().profile().getSettings(PWSPlugin.class).getString("settings", "");
        PWSPluginSettings set = null;
        try {
            set = PWSPluginSettings.fromJsonString(settingsStr);
        } catch (RuntimeException e) {
            ReportingUtils.logError(e); //Sometimes when we change the code we are unable to load old settings. Don't let that prevent things from starting up.
        }
        if (set==null) {
            Globals.mm().logs().logMessage("PWS Plugin: no settings found in user profile.");
        }
        return set;
    }
    
    public static void addPropertyChangeListener(PropertyChangeListener l) {
        instance().pcs.addPropertyChangeListener(l);
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener l) {
        instance().pcs.removePropertyChangeListener(l);
    }
            
    public static Studio mm() {
        return instance().studio_;
    }
    
    public static CMMCore core() {
        return instance().studio_.core();
    }
    
    public static AcquisitionManager acqManager() {
        return instance().acqMan_;
    }
    
    public static PluginFrame frame() {
        return instance().frame;
    }
    
    public static void setHardwareConfigurationSettings(HWConfigurationSettings configg) {
        try {
            instance().config.dispose(); //If we don't do this then the object will still hang around.
            instance().config = new HWConfiguration(configg);
            instance().pcs.firePropertyChange("config", null, instance().config);
            
            //Validate the hardware state
            List<String> errs = instance().config.validate();
            if (!errs.isEmpty()) {
                String msg = String.format("The following configuration errors were detected:\n %s", String.join("\n", errs));
                JOptionPane.showMessageDialog(instance.frame, msg); 
            }
        } catch (MMDeviceException e) {
            Globals.mm().logs().showError(e);
        }
    }
    
    public static HWConfiguration getHardwareConfiguration() {
        return instance().config;
    }
    
}