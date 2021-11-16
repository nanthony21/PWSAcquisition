package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionsequencer.Sequencer;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFactoryManager;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.SequencerUI;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.configurations.HWConfiguration;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.utils.PWSLogger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import mmcorej.CMMCore;
import org.micromanager.AutofocusPlugin;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * This class is used to provide access to various parts of the plugin in a way that is practically
 * global. This stops us from having to pass them around as variables everywhere. Probably not a
 * good idea in hindsight.
 */
public class Globals {
   private static Globals instance = null; //A singleton instance of the class is stored here.
   private Studio studio_ = null;
   private AcquisitionManager acqMan_;
   private PWSLogger logger_;
   private HWConfiguration config;
   private PluginFrame frame;
   private SequencerUI.API sequencer;
   private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

   private Globals() {}

   public static Globals instance() {
      if (instance == null) {
         instance = new Globals();
      }
      return instance;
   }

   /**
    * This must be called before anything else to initialize all the variables.
    * @param studio
    */
   public static void init(Studio studio) {
      instance().studio_ = studio;
      try {
         instance().logger_ = new PWSLogger(studio);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      instance().acqMan_ = new AcquisitionManager();
      instance().frame = new PluginFrame();
      instance().sequencer = instance().frame.getSequencePanel().getAPI();
      try {
         instance().config = new HWConfiguration(
               new HWConfigurationSettings()); //Set these even though they should be overridden when the settings are loaded.
      } catch (MMDeviceException e) {
         throw new RuntimeException(e);
      }

      PWSPluginSettings settings = Globals.loadSettings();
      instance().frame.populateFields(settings);
      if (settings != null) {
         Globals.setHardwareConfigurationSettings(settings.hwConfiguration);
      }
   }

   /**
    * Save plugin settings to Micro-Manager's settings system.
    * @param settings
    */
   public static void saveSettings(PWSPluginSettings settings) {
      Globals.mm().profile().getSettings(PWSPlugin.class)
            .putString("settings", settings.toJsonString());
   }

   /**
    * Load the saved settings.
    * @return The loaded settings.
    */
   private static PWSPluginSettings loadSettings() {
      String settingsStr = Globals.mm().profile().getSettings(PWSPlugin.class)
            .getString("settings", "");
      PWSPluginSettings set = null;
      try {
         set = PWSPluginSettings.fromJsonString(settingsStr);
      } catch (RuntimeException e) {
         ReportingUtils.logError(
               e); //Sometimes when we change the code we are unable to load old settings. Don't let that prevent things from starting up.
      }
      if (set == null) {
         Globals.mm().logs().logMessage("PWS Plugin: no settings found in user profile.");
      }
      return set;
   }

   /**
    * Objects can listen for when a global property has been changed. Right now only `config` is handled.
    * @param l The listener
    */
   public static void addPropertyChangeListener(PropertyChangeListener l) {
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

   public static SequencerUI.API sequencer() {
      return instance().sequencer;
   }

   /**
    * Update the hardware configuration with new settings. Fires an event to property change listeners.
    * @param configg
    */
   public static void setHardwareConfigurationSettings(HWConfigurationSettings configg) {
      try {
         instance().config.dispose(); //If we don't do this then the object will still hang around.
         instance().config = new HWConfiguration(configg);
         instance().pcs.firePropertyChange("config", null, instance().config);
      } catch (MMDeviceException e) {
         Globals.mm().logs().showError(e);
      }

   }

   /**
    * First do a coarse autofocus for maximum brightness. then do a software autofocus for sharpness.
    * Return two doubles, the final autofocus `result` (should be a z value) and the `score`.
    * Autofocus process will use the current camera exposure time.
    * @return
    */
   public static double[] softwareAutoFocus() {
      boolean liveWasOn = Globals.mm().live().isLiveModeOn();
      if (liveWasOn) {
         Globals.mm().live().setLiveModeOn(false);
      }

      Globals.mm().getAutofocusManager().setAutofocusMethodByName("OughtaFocus");
      AutofocusPlugin oughta = Globals.mm().getAutofocusManager().getAutofocusMethod();
      double result;
      double score;

      //Configure to search by image brightness
      try {
         oughta.setPropertyValue("SearchRange_um", "200");
         oughta.setPropertyValue("Tolerance_um", "1");
         oughta.setPropertyValue("CropFactor", "1");
         oughta.setPropertyValue("Exposure", String.valueOf(Globals.getHardwareConfiguration().getActiveConfiguration().camera().getExposure()));
         oughta.setPropertyValue("ShowImages", "No");
         oughta.setPropertyValue("Maximize", "Mean");
         oughta.setPropertyValue("Channel", "");
         oughta.fullFocus();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      //Configure to search by image sharpness over a smaller range and tighter tolerance.
      try {
         oughta.setPropertyValue("SearchRange_um", "20");
         oughta.setPropertyValue("Tolerance_um", "0.1");
         oughta.setPropertyValue("CropFactor", "1");
         oughta.setPropertyValue("Exposure", String.valueOf(Globals.getHardwareConfiguration().getActiveConfiguration().camera().getExposure()));
         oughta.setPropertyValue("ShowImages", "No");
         oughta.setPropertyValue("Maximize", "Redondo");
         oughta.setPropertyValue("Channel", "");
         result = oughta.fullFocus();
         score = oughta.getCurrentFocusScore();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      if (liveWasOn) {
         Globals.mm().live().setLiveModeOn(true);
      }
      double[] ans = {result, score};
      return ans;
   }
}