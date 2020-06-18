/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.RootStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.io.IOException;
import java.lang.reflect.Type;
import org.micromanager.PositionList;
import org.micromanager.PropertyMap;
import org.micromanager.data.internal.DefaultCoords;
import org.micromanager.internal.propertymap.PropertyMapJSONSerializer;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class GsonUtils {

    private static final GsonBuilder gsonBuilder = new GsonBuilder()
            .registerTypeHierarchyAdapter(PositionList.class, new PositionListGson())
            .registerTypeHierarchyAdapter(DefaultCoords.class, new CoordGson())
            .setPrettyPrinting()
            .serializeNulls(); //Without `serializeNulls` null fields will be skipped, then we json is loaded the default values will be used instead of null.

    public static GsonBuilder builder() {
        return gsonBuilder;
    }
    
    public static Gson getGson() {
        return gsonBuilder.create();
    }
}

//Gson adapters for Micro-Manager built-in classes that use PropertyMaps.
final class PositionListGson implements JsonDeserializer<PositionList>, JsonSerializer<PositionList> {
    @Override
    public PositionList deserialize(final JsonElement jsonElement, final java.lang.reflect.Type type, final JsonDeserializationContext context) throws JsonParseException {
        PropertyMap pmap = PropertyMapJSONSerializer.fromGson(jsonElement);
        PositionList list = new PositionList();
        try {
            list.replaceWithPropertyMap(pmap);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
        return list;
    }

    @Override
    public JsonElement serialize(PositionList list, Type type, JsonSerializationContext jsc) {
        PropertyMap pmap = list.toPropertyMap();
        return PropertyMapJSONSerializer.toGson(pmap);
    }
}


final class CoordGson implements JsonDeserializer<DefaultCoords>, JsonSerializer<DefaultCoords> {
    @Override
    public DefaultCoords deserialize(final JsonElement jsonElement, final java.lang.reflect.Type type, final JsonDeserializationContext context) throws JsonParseException {
        PropertyMap pmap = PropertyMapJSONSerializer.fromGson(jsonElement);
        return (DefaultCoords) DefaultCoords.fromPropertyMap(pmap);
    }

    @Override
    public JsonElement serialize(DefaultCoords coords, Type type, JsonSerializationContext jsc) {
        PropertyMap pmap = coords.toPropertyMap();
        return PropertyMapJSONSerializer.toGson(pmap);
    }
}
