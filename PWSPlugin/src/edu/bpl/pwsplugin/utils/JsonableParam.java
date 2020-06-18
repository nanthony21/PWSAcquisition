
package edu.bpl.pwsplugin.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */


public class JsonableParam {   
    //This base class allows it's subclasses to work more smoothly with Gson, they will Jsonified based on their runtime type rather than their declared type.
    //Note: Subclasses of this can not have any attributes names `type`. if they do they won't work with the runtime type adapter.
    private static final RuntimeTypeAdapterFactory<JsonableParam> adapter = RuntimeTypeAdapterFactory.of(JsonableParam.class);

    private static final HashSet<Class<?>> registeredClasses= new HashSet<Class<?>>();
            
    static {
        GsonUtils.builder().registerTypeAdapterFactory(adapter);
    }

    public static synchronized void registerClass(Class<? extends JsonableParam> c) {
        if (!registeredClasses.contains(c)) {
            registeredClasses.add(c);
            adapter.registerSubtype(c);
        }
    }
    
    public JsonableParam() {
        this.registerClass(this.getClass()); //Automatically register the class at instantiation. Really though we want to explicitly register each class elsewhere, otherwise Gson won't work for the class until the first time it is instantiated.
    }
        
    public JsonableParam copy() {
        return JsonableParam.fromJson(this.toJsonString(), this.getClass());
    }
    
    public String toJsonString() {
        Gson gson = GsonUtils.getGson();
        String json = gson.toJson(this);
        return json;
    }
    
    public JsonObject toJsonObject() {
        Gson gson = GsonUtils.getGson();
        return (JsonObject) gson.toJsonTree(this);
    }
    
    public void toJsonFile(String path) throws IOException {
        //Saves to a JSON file.
        FileWriter writer = new FileWriter(path);
        writer.write(this.toJsonString());
        writer.close();
    }
    
    public static JsonableParam fromJsonFile(String path, Class clazz) throws FileNotFoundException {
        Gson gson = GsonUtils.getGson();
        FileReader reader;
        reader = new FileReader(path);
        return (JsonableParam) gson.fromJson(reader, clazz);
    }
    
    public static  JsonableParam fromJson(String jsonStr, Class clazz) {
        Gson gson = GsonUtils.getGson();
        return (JsonableParam) gson.fromJson(jsonStr, clazz);
    }
    
    public static JsonableParam fromJson(JsonObject obj, Class clazz) {
        Gson gson = GsonUtils.getGson();
        return (JsonableParam) gson.fromJson(obj, clazz);
    }
    
    @Override
    public boolean equals(Object o) { //Determine equality based on the json string representation of the class.
        if (o == null) {
            return false;
        } else if (this.getClass()!=o.getClass()) {
            return false;
        } else {
            return this.toJsonString().equals(((JsonableParam) o).toJsonString());
        }
    }

    @Override
    public int hashCode() {
        int hash = this.toJsonString().hashCode();
        return hash;
    }
}
