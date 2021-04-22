package edu.bpl.pwsplugin.hardware.configurations;

import com.google.common.collect.Iterables;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


public class HWConfiguration {

   HWConfigurationSettings settings;
   Map<String, ImagingConfiguration> imConfigs;
   private ImagingConfiguration activeConf_;


   public HWConfiguration(HWConfigurationSettings settings) throws MMDeviceException {
      this.settings = settings;
      imConfigs = new HashMap<>();
      List<String> errs = new ArrayList<>();
      for (int i = 0; i < settings.configs.size(); i++) {
         ImagingConfigurationSettings s = settings.configs.get(i);
         try {
            imConfigs.put(s.name, ImagingConfiguration.getInstance(s));
         } catch (MMDeviceException e) {
            Globals.logger().logError(e);
            errs.add(s.name);
         }
      }
      if (errs.size() > 0) {
         String configNames = errs.stream().collect(Collectors.joining(", "));
         Globals.mm().logs().showMessage(
               String.format("Failed to initialize the following imaging configurations: %s",
                     configNames));
      }
      if (imConfigs.size() > 0) {
         ImagingConfiguration conf = Iterables.get(imConfigs.values(), 0);
         this.activeConf_ = conf;
         this.activateImagingConfiguration(
               conf);//We must always have one, and only one, active configuration
      }
   }

   public HWConfigurationSettings getSettings() {
      return this.settings;
   }

   public ImagingConfiguration getImagingConfigurationByName(String name)
         throws NoSuchElementException {
      ImagingConfiguration conf = this.imConfigs.get(name);
      if (conf == null) {
         throw new NoSuchElementException(
               "Could not find Imaging Configuration by the name " + name);
      }
      return conf;
   }

   public List<ImagingConfiguration> getImagingConfigurations() {
      return new ArrayList<>(this.imConfigs.values());
   }

   public ImagingConfiguration getActiveConfiguration() {
      return activeConf_;
   }

   public final void activateImagingConfiguration(ImagingConfiguration conf)
         throws MMDeviceException {
      this.activeConf_.deactivateConfiguration();
      conf.activateConfiguration();
      this.activeConf_ = conf;
   }

   public List<String> validate() throws MMDeviceException {
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
