///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//

package edu.bpl.pwsplugin.acquisitionsequencer.steps;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFactoryManager;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.tree.CopyableMutableTreeNode;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Base class for a single step in the acquisition sequencer.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public abstract class Step<T extends JsonableParam> extends CopyableMutableTreeNode {
   protected T settings;
   private final String stepType;
   private static final AtomicInteger COUNTER = new AtomicInteger(); //This static counter makes sure that each Step object has it's own uid during runtime.
   private final Integer uid = COUNTER.getAndIncrement();
   private boolean isRunning_ = false;


   /**
    * Create a mew instance.
    * @param settings The settings for the instance of the step to be created
    * @param type The string indicating the "Type" of this step. TODO why is this needed.
    */
   public Step(T settings, String type) {
      super();
      this.stepType = type;
      this.setSettings(settings);
   }

   /**
    * Copy constructor
    * @param step The step instance to copy
    */
   public Step(Step<T> step) { //copy constructor
      this((T) step.settings.copy(), step.stepType);
   }

   /**
    *
    * @return The unique ID of this object. It is unique during a single session of the program. ID's reset when the software is restarted.
    */
   public Integer getID() {
      return this.uid;
   }

   /**
    *
    * @return A string indicating which type of sequence step this is.
    */
   public final String getType() {
      return stepType;
   }

   /**
    *
    * @return Use json to safely copy the object
    */
   @Override
   public Step<T> copy() {
      Gson gson = GsonUtils.getGson();
      return (Step<T>) gson.fromJson(gson.toJson(this), this.getClass());
   }

   /**
    *
    * @return A copy of the internal settings of this instance.
    */
   public final T getSettings() {
      return (T) settings.copy();
   }

   /**
    * Set new settings for this step.
    * @param settings
    */
   public final void setSettings(T settings) {
      this.settings = settings;
   }

   /**
    *    Return  function to run for this step during execution. Does not include callbacks and
    *    mandatory changes to the status object which are handled automatically by `getFunction`.
    *    This should initialize any variables that are used for context during runtime.
    */
   protected abstract SequencerFunction getStepFunction(List<SequencerFunction> callbacks);

   /**
    * Return a function that simulates how folder usage and cell number changes through the run.
    */
   protected abstract SimFn getSimulatedFunction();

   /**
    *
    * @return A list of any errors for this step.
    */
   public abstract List<String> validate();

   /**
    * A single instance of this class is passed between the simulation functions to keep track of multiple parameters.
    */
   public static class SimulatedStatus {
      public Integer cellNum = 1; // The "Cell{X}" number that the acquisition is on.
      // A list of file paths that will be saved. Used to determine if there are any file conflicts.
      public List<String> requiredPaths = new ArrayList<>();
      public String workingDirectory = ""; // The current folder we are in.
   }

   /**
    * Recieves a SimulatedStatus and returns the same object.
    */
   @FunctionalInterface
   public interface SimFn extends Function<SimulatedStatus, SimulatedStatus> {}

   /**
    * Subclasses can override to define a callback function that will be run before each child step.
    * For example, the optical focus lock checks that PFS is still locked. If not,
    * then it goes through a search routine.
    * @return
    */
   protected SequencerFunction getCallback() {
      return null;
   }

   /**
    *
    * @param rcvdCallbacks  A list of callbacks which will run before this step's functionality runs.
    * @return The function which will be executed to run this step.
    */
   public final SequencerFunction getFunction(List<SequencerFunction> rcvdCallbacks) {
      final List<SequencerFunction> callbacks = new ArrayList<>(
            rcvdCallbacks); //To avoid confusion due to the mutable nature of the List we make sure that each time a sequencer function is created it has it's own copy of callbacks to work with, other wise all steps end up sharing a single callback list.
      if (this.getCallback() != null) {
         callbacks.add(this.getCallback());
      }
      SequencerFunction stepFunc = this.getStepFunction(callbacks);
      return (status) -> {
         //Update the status object with information about the current step.
         status.coords()
               .moveDownTree(Step.this); //Append this step to the end of our coordinate path.
         status.allowPauseHere(); //Every step can be paused before starting.
         if (Thread.currentThread()
               .isInterrupted()) { //This makes sure that every step allows for cancellation.
            throw new InterruptedException("Thread interrupt detected");
         }
         //Run any callbacks that have been set for this step.
         for (SequencerFunction func : rcvdCallbacks) {
            status = func.apply(status);
         }
         //Run the function for this step subclass.
         isRunning_ = true;
         status = stepFunc.apply(status);
         isRunning_ = false;
         status.coords().moveUpTree(); //Set the path back to where it was as we exit this step
         return status;
      };
   }

   public final boolean isRunning() { return isRunning_; }
}


