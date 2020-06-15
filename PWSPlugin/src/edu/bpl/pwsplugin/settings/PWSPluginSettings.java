
package edu.bpl.pwsplugin.settings;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.RootStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import jdk.nashorn.internal.objects.Global;


/**
 *
 * @author nick
 */
public class PWSPluginSettings extends JsonableParam {
    //This is just a container for all the other settings. this is the main object that gets
    //passed around, saved, loaded, etc.
    public HWConfigurationSettings hwConfiguration = new HWConfigurationSettings();
    public AcquireCellSettings acquisitionSettings = new AcquireCellSettings();
    public RootStep sequenceRoot = (RootStep) SequencerConsts.getFactory(SequencerConsts.Type.ROOT).createStep();
    public String saveDir = "";
    public int cellNum = 1;

    public static PWSPluginSettings fromJsonString(String str) {
        return (PWSPluginSettings) JsonableParam.fromJson(str, PWSPluginSettings.class);
    }
    
    @Override
    public String toJsonString() {
        Gson gson = GsonUtils.getGson();
        JsonObject obj = new JsonObject();
        obj.add("hwConfiguration", gson.toJsonTree(hwConfiguration));
        obj.add("acquisitionSettings", gson.toJsonTree(acquisitionSettings));
        obj.add("sequenceRoot", gson.toJsonTree(sequenceRoot));
        obj.add("saveDir", gson.toJsonTree(saveDir));
        obj.add("cellNum", gson.toJsonTree(cellNum));
        return gson.toJson(obj);
    }
    
    public static void registerGsonType() { //This must be called for GSON loading/saving to work.
        GsonUtils.builder().registerTypeAdapter(PWSPluginSettings.class, new PWSSettingsSerializer());
        GsonUtils.builder().registerTypeAdapter(PWSPluginSettings.class, new PWSSettingsDeserializer());
    }
}

final class PWSSettingsDeserializer implements JsonDeserializer<PWSPluginSettings> {
    @Override
    public PWSPluginSettings deserialize(final JsonElement jsonElement, final java.lang.reflect.Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject obj = jsonElement.getAsJsonObject();
        PWSPluginSettings settings = new PWSPluginSettings();
        settings.hwConfiguration = context.deserialize(obj.get("hwConfiguration"), HWConfigurationSettings.class);
        settings.acquisitionSettings = context.deserialize(obj.get("acquisitionSettings"), AcquireCellSettings.class);
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
}

final class PWSSettingsSerializer implements JsonSerializer<PWSPluginSettings> {
    @Override
    public JsonElement serialize(PWSPluginSettings settings, Type type, JsonSerializationContext jsc) {
        if (!type.getTypeName().equals(settings.getClass().getTypeName())) { //Not sure if this is even possible.
            throw new RuntimeException("huh?");
        }
        JsonObject obj = new JsonObject();
        obj.add("hwConfiguration", jsc.serialize(settings.hwConfiguration, HWConfigurationSettings.class));
        obj.add("acquisitionSettings", jsc.serialize(settings.acquisitionSettings, AcquireCellSettings.class));
        obj.add("sequenceRoot", jsc.serialize(settings.sequenceRoot, Step.class));
        obj.add("saveDir", jsc.serialize(settings.saveDir));
        obj.add("cellNum", jsc.serialize(settings.cellNum));
        return obj;
    }
}