/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.bpl.pwsplugin.acquisitionsequencer;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the top level container for an instance of the sequencer.
 * Keeps track of all of the StepFactories registered with this instance.
 *
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class SequencerFactoryManager {
   private final Map<String, StepFactory> registry_ = new HashMap<>();

   /**
    * Create a new instance.
    */
   public SequencerFactoryManager() {
      for (SequencerConsts.Type stepType : SequencerConsts.Type.values()) {
         registerFactory(stepType.name(), SequencerConsts.getFactory(stepType.name()));
      }

      SequencerPlugin defaultPlugin = new DefaultSequencerPlugin(); // TODO replace this hardcoded registration with a plugin discovery service.
      for (String stepType : defaultPlugin.getAvailableStepNames()) {
         registerFactory(stepType, defaultPlugin.getFactory(stepType));
      }

      // Register this factor with GSON for Step deserialization
      //This custom adapter enables Steps to be Jsonified by GSON even though they have a circular parent/child reference.
      TypeAdapterFactory factory = new TypeAdapterFactory() {
         @Override
         @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
         public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (Step.class.isAssignableFrom(type.getRawType())) { //Allow subtypes to use this factory.
               return (TypeAdapter<T>) new StepTypeAdapter(gson, SequencerFactoryManager.this);
            }
            return null;
         }
      };
      GsonUtils.builder().registerTypeAdapterFactory(factory);

      registerSettingsWithGSON();
   }

   /**
    * Will return null if a factory hasn't been registered for this typeName.
    *
    * @param typeName The name the factory was registered under
    * @return A StepFactory instance
    */
   public StepFactory getFactory(String typeName) {
      return registry_.get(typeName);
   }

   /**
    *
    * @return A List of names of factory types that have been registered.
    */
   public Set<String> getRegisteredFactories() {
      return registry_.keySet();
   }

   /**
    * Make sure the settings for all registered factories have been registered with the GSON type adapter.
    */
   private void registerSettingsWithGSON() {
      for (Map.Entry<String, StepFactory> entry : registry_.entrySet()) {
         JsonableParam.registerClass(entry.getValue().getSettings());
      }
   }

   private void registerFactory(String typeName, StepFactory factoryInstance)
         throws IllegalArgumentException {
      if (registry_.containsKey(typeName)) {
         throw new IllegalArgumentException(
               String.format("A StepFactory of type %s has already been registered.", typeName));
      }
      registry_.put(typeName, factoryInstance);
   }
}


/**
 * Defines how GSON should write/read Step objects to JSON.
 */
class StepTypeAdapter extends TypeAdapter<Step> {
   private final Gson gson;
   private final SequencerFactoryManager sequencerFactoryManager;

   public StepTypeAdapter(Gson gson, SequencerFactoryManager sequencerFactoryManager) {
      this.gson = gson;
      this.sequencerFactoryManager = sequencerFactoryManager;
   }

   @Override
   public void write(JsonWriter out, Step step) throws IOException {
      out.beginObject();
      out.name("id");
      out.value(step.getID());
      out.name("stepType");
      out.value(step.getType());
      out.name("settings");
      JsonableParam settings = step.getSettings();
      gson.toJson(settings, settings.getClass(), out);
      if (step.getAllowsChildren()) {
         out.name("children");
         gson.toJson(Collections.list(step.children()), List.class, out); // recursion!
      }
      // No need to write node.getParent(), it would lead to infinite recursion.
      out.endObject();
   }

   @Override
   public Step read(JsonReader in) throws IOException {
      try {
         in.beginObject();
         if (!in.nextName().equals("id")) {
            throw new IOException("Json Parse Error");
         } //ID is determined at runtime don't load it.
         int id = in.nextInt(); //read the id to get rid of it.
         if (!in.nextName().equals("stepType")) {
            throw new IOException("Json Parse Error");
         } //This must be "stepType"
         String stepType = in.nextString();
         StepFactory factory = sequencerFactoryManager.getFactory(stepType);
         Step step = (Step) factory.createStep();
         if (!in.nextName().equals("settings")) {
            throw new IOException("Json Parse Error");
         }
         JsonableParam settings = gson
               .fromJson(in, factory.getSettings());
         step.setSettings(settings);
         if (step.getAllowsChildren()) {
            if (!in.nextName().equals("children")) {
               throw new IOException("Json Parse Error");
            }
            in.beginArray();
            while (in.hasNext()) {
               step.add(read(in)); // recursion! this did also set the parent of the child-node
            }
            in.endArray();
         }
         in.endObject();
         return step;
      } catch (IOException | JsonIOException | IllegalStateException ioe) {
         while (in.hasNext()) {
            in.skipValue();
         } //Read out the rest of the failed json object before returning.
         in.endObject();
         //Rather than allow an exception to break the whole loading process just insert the
         // special "Broken step" where the error occurred.
         return (Step) SequencerConsts.getFactory(SequencerConsts.Type.BROKEN.name()).createStep();
      }
   }
}