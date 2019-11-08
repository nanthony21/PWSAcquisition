
package edu.bpl.pwsplugin.utils;

import com.google.gson.Gson;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import java.io.FileReader;
import java.util.HashSet;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */


public class JsonableParam {    
    private static final RuntimeTypeAdapterFactory<JsonableParam> adapter = 
            RuntimeTypeAdapterFactory.of(JsonableParam.class);

    private static final HashSet<Class<?>> registeredClasses= new HashSet<Class<?>>();
            
    static {
        GsonUtils.registerType(adapter);
    }

    public static synchronized void registerClass(Class<? extends JsonableParam> c) {
        if (!registeredClasses.contains(c)) {
            registeredClasses.add(c);
            adapter.registerSubtype(c);
        }
    }
    
    public JsonableParam() {
        this.registerClass(this.getClass());
    }
            
    
    public String toJsonString() {
        Gson gson = GsonUtils.getGson();
        String json = gson.toJson(this);
        return json;
    }
    
    public static JsonableParam fromJson(String path, Class clazz) {
        Gson gson = GsonUtils.getGson();
        FileReader reader;
        try {
            reader = new FileReader(path);
        } catch (Exception e) {
            ReportingUtils.showError(e);
            ReportingUtils.logError(e);
            return null;
        }
        return (JsonableParam) gson.fromJson(reader, clazz);
    }
    
    public static  JsonableParam fromJsonString(String jsonStr, Class clazz) {
        Gson gson = GsonUtils.getGson();
        return (JsonableParam) gson.fromJson(jsonStr, clazz);
    }
}
