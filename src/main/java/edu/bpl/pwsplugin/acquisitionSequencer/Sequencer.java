/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.acquisitionsequencer.UI.SequencerUI;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the top level container for an instance of the sequencer.
 *
 * @author nick
 */
public class Sequencer {
   private final FactoryRegistry registry_ = new FactoryRegistry();

   public Sequencer() {

      //TODO plugin loading instead of hard coding.
      for (SequencerConsts.Type stepType : SequencerConsts.Type.values()) {
         registry_.registerFactory(stepType.name(), SequencerConsts.getFactory(stepType.name()));
      }

      SequencerPlugin defaultPlugin = new DefaultSequencerPlugin();
      for (String stepType : defaultPlugin.getAvailableStepNames()) {
         registry_.registerFactory(stepType, defaultPlugin.getFactory(stepType));
      }
   }

   public StepFactory getFactory(String typeName) {
      return registry_.getFactory(typeName);
   }

   public Set<String> getRegisteredFactories() {
      return registry_.getRegisteredNames();
   }
}


class FactoryRegistry {
   private final Map<String, StepFactory> factoryMap = new HashMap<>();

   public void registerFactory(String typeName, StepFactory factoryInstance)
         throws IllegalArgumentException {
      if (factoryMap.containsKey(typeName)) {
         throw new IllegalArgumentException(
               String.format("A StepFactory of type %s has already been registered.", typeName));
      }
      factoryMap.put(typeName, factoryInstance);
   }

   /**
    * Will return null if a factory hasn't been registered for this typeName.
    * 
    * @param typeName The name the factory was registered under
    * @return A StepFactory instance
    */
   public StepFactory getFactory(String typeName) {
      return factoryMap.get(typeName);
   }

   /**
    *
    * @return A set of all registered factory names
    */
   public Set<String> getRegisteredNames() {
      return factoryMap.keySet();
   }

   /**
    * Make sure all registered factories have been registered with the GSON type adapter.
    */
   public void registerGson() {
      for (Map.Entry<String, StepFactory> entry : factoryMap.entrySet()) {
         entry.getValue().registerGson();
      }
   }
}