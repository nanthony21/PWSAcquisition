
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
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */


public class JsonableParam {
   //This base class allows it's subclasses to work more smoothly with Gson, they will be automatically Jsonified based on their runtime type rather than their declared type.
   //Note: Subclasses of this can not have any attributes of name `type`. if they do they won't work with the runtime type adapter.

   private static final RuntimeTypeAdapterFactory<JsonableParam> adapter = RuntimeTypeAdapterFactory
         .of(JsonableParam.class);
   private static final HashSet<Class<?>> registeredClasses = new HashSet<Class<?>>(); //Keeps track of which classes have been registered.

   static {
      GsonUtils.builder()
            .registerTypeAdapterFactory(adapter); //Register the runtime type adapter with Gson.
   }

   public static synchronized final void registerClass(
         Class<? extends JsonableParam> c) { //Each subclass of JsonableParam must be registered with this method or loading from JSON will not work.
      if (!registeredClasses.contains(c)) {
         registeredClasses.add(c);
         adapter.registerSubtype(c);
      }
   }

   public JsonableParam() {
      this.registerClass(
            this.getClass()); //Automatically register the class at instantiation. Really though we want to explicitly register each class elsewhere, otherwise Gson won't work for the class until the first time it is instantiated.
   }

   public JsonableParam copy() { //Create a deep copy of the object by serializing and then deserializing.
      return JsonableParam.fromJson(this.toJsonString(), this.getClass());
   }

   public String toJsonString() {  //Return the object as a JSON string.
      Gson gson = GsonUtils.getGson();
      String json = gson.toJson(this);
      return json;
   }

   public JsonObject toJsonObject() { //Return the object as a GSON "JsonObject"
      Gson gson = GsonUtils.getGson();
      return (JsonObject) gson.toJsonTree(this);
   }

   public void toJsonFile(String path)
         throws IOException { //Save the object directly to a text file.
      //Saves to a JSON file.
      FileWriter writer = new FileWriter(path);
      writer.write(this.toJsonString());
      writer.close();
   }

   public static JsonableParam fromJsonFile(String path, Class clazz)
         throws FileNotFoundException { //Load a new instance of this class from a JSON file.
      Gson gson = GsonUtils.getGson();
      FileReader reader;
      reader = new FileReader(path);
      return (JsonableParam) gson.fromJson(reader, clazz);
   }

   public static JsonableParam fromJson(String jsonStr,
         Class clazz) { //Load a new instance of this class from a JSON string.
      Gson gson = GsonUtils.getGson();
      return (JsonableParam) gson.fromJson(jsonStr, clazz);
   }

   public static JsonableParam fromJson(JsonObject obj,
         Class clazz) { //Load a new instance of this class from a GSON "JsonObject"
      Gson gson = GsonUtils.getGson();
      return (JsonableParam) gson.fromJson(obj, clazz);
   }

   @Override
   public boolean equals(
         Object o) { //Determine equality based on the json string representation of the class.
      if (o == null) {
         return false;
      } else if (this.getClass() != o.getClass()) {
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
