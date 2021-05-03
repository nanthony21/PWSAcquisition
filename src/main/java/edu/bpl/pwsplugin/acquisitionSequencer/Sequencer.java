/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.acquisitionsequencer.UI.SequencerUI;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultPlugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the top level container for an instance of the sequencer.
 *
 * @author nick
 */
public class Sequencer {

   private final SequencerUI ui_;
   private final FactoryRegistry registry_ = new FactoryRegistry();

   public Sequencer() { //TODO plugin loading instead of hard coding.
      SequencerPlugin defaultPlugin = new DefaultSequencerPlugin();
      for (String stepType : defaultPlugin.getAvailableStepNames()) {
         registry_.registerFactory(stepType, SequencerConsts.getFactory(stepType));
      }

      ui_ = new SequencerUI(
            this); // Important that the registry is all set before the UI is created.
   }

   public SequencerUI ui() {
      return ui_;
   }

   public StepFactory getFactory(String typeName) {
      return registry_.getFactory(typeName);
   }
}


class FactoryRegistry {

   private final Map<String, StepFactory> REGISTRY = new HashMap<>();

   public void registerFactory(String typeName, StepFactory factoryInstance)
         throws IllegalArgumentException {
      if (REGISTRY.containsKey(typeName)) {
         throw new IllegalArgumentException(
               String.format("A StepFactory of type %s has already been registered.", typeName));
      }
      REGISTRY.put(typeName, factoryInstance);
   }

   public StepFactory getFactory(
         String typeName) { // Will return null if a factory hasn't been registered for this typeName
      return REGISTRY.get(typeName);
   }

   //Make sure all registered facotories have been registered with the GSON type adapter.
   public void registerGson() {
      for (Map.Entry<String, StepFactory> entry : REGISTRY.entrySet()) {
         entry.getValue().registerGson();
      }
   }

}