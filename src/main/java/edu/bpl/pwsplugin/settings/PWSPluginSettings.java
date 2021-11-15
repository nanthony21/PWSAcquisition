package edu.bpl.pwsplugin.settings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.RootStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.lang.reflect.Type;


/**
 * This is just a container for all the other settings. this is the main object that gets
 * passed around, saved, loaded, etc.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class PWSPluginSettings extends JsonableParam {
   public HWConfigurationSettings hwConfiguration = new HWConfigurationSettings();
   public AcquireCellSettings acquisitionSettings = new AcquireCellSettings();
   public RootStep sequenceRoot = new RootStepFactory().createStep();
   public String saveDir = "";
   public int cellNum = 1;

   public static PWSPluginSettings fromJsonString(String str) {
      return (PWSPluginSettings) JsonableParam.fromJson(str, PWSPluginSettings.class);
   }

   /**
    * This must be called for GSON loading/saving to work.
    */
   public static void registerGsonType() {
      GsonUtils.builder().registerTypeAdapter(PWSPluginSettings.class, new PWSSettingsGson());
   }
}

final class PWSSettingsGson implements JsonDeserializer<PWSPluginSettings>,
      JsonSerializer<PWSPluginSettings> {

   /**
    * This defines custom behavior to save and load these settings using GSON.
    * @param jsonElement
    * @param type
    * @param context
    * @return
    * @throws JsonParseException
    */
   @Override
   public PWSPluginSettings deserialize(final JsonElement jsonElement,
         final java.lang.reflect.Type type, final JsonDeserializationContext context)
         throws JsonParseException {
      final JsonObject obj = jsonElement.getAsJsonObject();
      PWSPluginSettings settings = new PWSPluginSettings();
      settings.hwConfiguration = context
            .deserialize(obj.get("hwConfiguration"), HWConfigurationSettings.class);
      settings.acquisitionSettings = context
            .deserialize(obj.get("acquisitionSettings"), AcquireCellSettings.class);
      try {
         settings.sequenceRoot = context.deserialize(obj.get("sequenceRoot"), Step.class);
      } catch (JsonParseException e) {
         Globals.mm().logs().logError(e);
         settings.sequenceRoot = new RootStep();
      }
      settings.saveDir = obj.getAsJsonPrimitive("saveDir").getAsString();
      settings.cellNum = obj.getAsJsonPrimitive("cellNum").getAsInt();
      return settings;
   }

   @Override
   public JsonElement serialize(PWSPluginSettings settings, Type type,
         JsonSerializationContext jsc) {
      if (!type.getTypeName()
            .equals(settings.getClass().getTypeName())) { //Not sure if this is even possible.
         throw new RuntimeException("huh?");
      }
      JsonObject obj = new JsonObject();
      obj.add("hwConfiguration",
            jsc.serialize(settings.hwConfiguration, HWConfigurationSettings.class));
      obj.add("acquisitionSettings",
            jsc.serialize(settings.acquisitionSettings, AcquireCellSettings.class));
      obj.add("sequenceRoot", jsc.serialize(settings.sequenceRoot, Step.class));
      obj.add("saveDir", jsc.serialize(settings.saveDir));
      obj.add("cellNum", jsc.serialize(settings.cellNum));
      return obj;
   }
}