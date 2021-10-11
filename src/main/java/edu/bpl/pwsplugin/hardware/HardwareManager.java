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
import java.util.Map.Entry;

public class HardwareManager {
   private final Map<JsonableParam, Device> map_ = new HashMap<>();

   private static final HardwareManager instance_ = new HardwareManager();

   public static HardwareManager instance() {
      return instance_;
   }

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

   /*private Device checkCached(JsonableParam settings) {
      return map_.getOrDefault(settings, null);
   }

   private void saveCache(JsonableParam settings, Device dev) {
      if (!map_.containsKey(settings)) {
         map_.put(settings, dev);
      }
   }*/

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Entry entry : map_.entrySet()) {
         sb.append(entry.getValue().toString());
         sb.append("\n");
      }
      return sb.toString();
   }
}
