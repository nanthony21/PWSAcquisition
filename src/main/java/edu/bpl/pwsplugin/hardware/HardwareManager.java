package edu.bpl.pwsplugin.hardware;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.HashMap;
import java.util.Map;

/**
 * Use this as the factory for any device implementation. Keeps track of all instances created in
 * order to avoid creating multiple instances for the same physical device.
 */
public class HardwareManager {
   private final Map<JsonableParam, Device> map_ = new HashMap<>();

   private static final HardwareManager instance_ = new HardwareManager();

   /**
    * Singleton instance.
    * @return
    */
   public static HardwareManager instance() {
      return instance_;
   }

   /**
    * If a device for these settings has already been created then return that. Otherwise create
    * a new one and cache it.
    *
    * @param settings The settings for the device to create.
    * @return
    */
   public Device getDevice(JsonableParam settings) {
      Device dev = map_.getOrDefault(settings, null);
      if (dev == null) {
         if (settings.getClass().isAssignableFrom(CamSettings.class)) {
            dev = Camera.getAutomaticInstance((CamSettings) settings);
         } else if (settings.getClass().isAssignableFrom(TranslationStage1dSettings.class)) {
            dev = TranslationStage1d.getAutomaticInstance();
         } else if (settings.getClass().isAssignableFrom(TunableFilterSettings.class)) {
            dev = TunableFilter.getAutomaticInstance((TunableFilterSettings) settings);
         } else if (settings.getClass().isAssignableFrom(IlluminatorSettings.class)) {
            dev = Illuminator.getAutomaticInstance((IlluminatorSettings) settings);
         } else {
            throw new RuntimeException(String.format("Didn't recognize class %s.",
                  settings.getClass()));
         }
         map_.put(settings, dev);
      }
      return dev;
   }
}
