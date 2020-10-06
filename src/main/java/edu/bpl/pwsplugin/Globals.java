
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.hardware.configurations.HWConfiguration;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.utils.PWSLogger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.JOptionPane;
import mmcorej.CMMCore;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;

public class Globals {
    //This class is used to provide access to various parts of the plugin in a way that is practically global. This stops us from having to pass them around as variables everywhere.
    
    private static Globals instance = null; //A singleton instance of the class is stored here.
    private Studio studio_ = null;
    private AcquisitionManager acqMan_;
    private PWSLogger logger_;
    private HWConfiguration config;
    private PluginFrame frame;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private Globals() {}
    
    private static Globals instance() {
        //Stores a reference to the singleton instance of this class
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }
    
    public static void init(Studio studio) {
        //This must be called before anything else to initialize all the variables.
        instance().studio_ = studio;
        try {
            instance().logger_ = new PWSLogger(studio);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    
    public static void saveSettings(PWSPluginSettings settings) { //Save plugin settings to Micro-Manager's settings system.
        Globals.mm().profile().getSettings(PWSPlugin.class).putString("settings", settings.toJsonString());
    }
    
    private static PWSPluginSettings loadSettings() { //Load and apply the saved settings.
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
    
    public static void addPropertyChangeListener(PropertyChangeListener l) { //Objects can listen for when a global property has been changed. Right now only `config` is handled.
        instance().pcs.addPropertyChangeListener(l);
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener l) {
        instance().pcs.removePropertyChangeListener(l);
    }
            
    public static Studio mm() { //Convenient way to access the instance of Micro-Manager Studio that we are running within.
        return instance().studio_;
    }
    
    public static CMMCore core() { // Convenient access to the MMCore for device control
        return instance().studio_.core();
    }
    
    public static AcquisitionManager acqManager() { //Access to the PWSPlugin Acquisition Manager.
        return instance().acqMan_;
    }
    
    public static PluginFrame frame() { //Access to the top level JFrame of the plugin.
        return instance().frame;
    }
    
    public static PWSLogger logger() { //Access to the logger object used to save log files.
        return instance().logger_;
    }
    
    public static HWConfiguration getHardwareConfiguration() { //Access to the current hardware configuration.
        return instance().config;
    }
    
    public static void setHardwareConfigurationSettings(HWConfigurationSettings configg) { //Update the hardware configuration with new settings. Fires an event to property change listeners.
        try {
            instance().config.dispose(); //If we don't do this then the object will still hang around.
            instance().config = new HWConfiguration(configg);
            instance().pcs.firePropertyChange("config", null, instance().config);
        } catch (MMDeviceException e) {
            Globals.mm().logs().showError(e);
        }
    }   
}