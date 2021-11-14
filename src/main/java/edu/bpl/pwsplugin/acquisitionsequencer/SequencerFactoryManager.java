/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is the top level container for an instance of the sequencer.
 *
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class SequencerFactoryManager {

   private final FactoryRegistry registry = new FactoryRegistry();

   /**
    *
    */
   public SequencerFactoryManager() {
      for (SequencerConsts.Type stepType : SequencerConsts.Type.values()) {
         registry.registerFactory(stepType.name(), SequencerConsts.getFactory(stepType.name()));
      }

      SequencerPlugin defaultPlugin = new DefaultSequencerPlugin();
      for (String stepType : defaultPlugin.getAvailableStepNames()) {
         registry.registerFactory(stepType, defaultPlugin.getFactory(stepType));
      }

      Step.registerGsonType(this);
      registerGson();
   }

   public StepFactory getFactory(String typeName) {
      return registry.getFactory(typeName);
   }

   public Set<String> getRegisteredFactories() {
      return registry.getRegisteredNames();
   }

   public void registerGson() {
      registry.registerGson();
   }


   private static class FactoryRegistry {

      private final Map<String, StepFactory> registry_ = new HashMap<>();

      public void registerFactory(String typeName, StepFactory factoryInstance)
            throws IllegalArgumentException {
         if (registry_.containsKey(typeName)) {
            throw new IllegalArgumentException(
                  String.format("A StepFactory of type %s has already been registered.", typeName));
         }
         registry_.put(typeName, factoryInstance);
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
       * @return A set of all registered factory names
       */
      public Set<String> getRegisteredNames() {
         return registry_.keySet();
      }

      /**
       * Make sure all registered factories have been registered with the GSON type adapter.
       */
      public void registerGson() {
         for (Map.Entry<String, StepFactory> entry : registry_.entrySet()) {
            entry.getValue().registerGson();
         }
      }
   }
}