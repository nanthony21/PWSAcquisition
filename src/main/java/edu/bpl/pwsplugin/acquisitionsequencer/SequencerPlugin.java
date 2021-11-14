/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import java.util.List;

/**
 * A plugin can implement this interface to define it's own steps for the sequencer to run.
 * @author Nick Anthony
 */
public interface SequencerPlugin {

   /**
    * Return a StepFactory associated with a given name returned by `getAvailableStepNames`.
    * @param name
    * @return
    */
   StepFactory getFactory(String name);

   /**
    * Return a list of the step type names handled by the factory.
    * @return
    */
   List<String> getAvailableStepNames();
}
