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
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.tree.CopyableMutableTreeNode;
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
 * @author nick
 */
public abstract class Step<T extends JsonableParam> extends CopyableMutableTreeNode {

   //Base class for a single step in the acquisition sequencer.
   protected T settings;
   private final String stepType;
   private static final AtomicInteger COUNTER = new AtomicInteger();
         //This static counter makes sure that each Step object has it's own uid during runtime.
   private final Integer uid = COUNTER.getAndIncrement();


   public Step(T settings, String type) {
      super();
      this.stepType = type;
      this.setSettings(settings);
   }


   public Step(Step step) { //copy constructor
      this((T) step.settings.copy(), step.stepType);
   }

   public Integer getID() {
      //Return the unique ID of this object. It is unique during a single session of the program. ID's reset when the software is restarted.
      return this.uid;
   }

   public final String getType() {
      //An string indicating which type of sequence step this is.
      return stepType;
   }

   @Override
   public Step copy() {
      //Use json to safely copy the object
      Gson gson = GsonUtils.getGson();
      return (Step) gson.fromJson(gson.toJson(this), this.getClass());
   }

   public final T getSettings() {
      return (T) settings.copy();
   }

   public final void setSettings(T settings) {
      this.settings = settings;
   }

   //Return  function to run for this step during execution. Does not include callbacks and mandatory changes to the status object which are handled automatically by `getFunction`. This should initialize any variables that are used for context during runtime.
   protected abstract SequencerFunction getStepFunction(List<SequencerFunction> callbacks);

   //return a function that simulates how folder usage and cell number changes through the run.
   protected abstract SimFn getSimulatedFunction();

   public abstract List<String> validate(); //Return a list of any errors for this step.

   public static class SimulatedStatus {

      //A single instance of this class is passed between the simulation functions to keep track of multiple parameters.
      public Integer cellNum = 1; // The "Cell{X}" number that the acquisition is on.
      public List<String> requiredPaths = new ArrayList<>();
            // A list of file paths that will be saved. Used to determine if there are any file conflicts.
      public String workingDirectory = ""; // The current folder we are in.
   }

   @FunctionalInterface
   public static interface SimFn extends Function<SimulatedStatus, SimulatedStatus> {

   } //Recieves a SimulatedStatus and returns the same object.

   protected SequencerFunction getCallback() {
      return null;
   } //Subclasses can override to define a callback function that will be run before each child step. For example, the optical focus lock checks that PFS is still locked. If not, then it goes through search routine.

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
         status = stepFunc.apply(status);
         status.coords().moveUpTree(); //Set the path back to where it was as we exit this step
         return status;
      };
   }

   @Override
   public String toString() { //this determines how it is labeled in a JTree
      return SequencerConsts.getFactory(this.getType()).getName();
   }

   public static void registerGsonType() { //This must be called for GSON loading/saving to work.
      GsonUtils.builder().registerTypeAdapterFactory(StepTypeAdapter.FACTORY);
   }

   public void saveToJson(String savePath) throws IOException {
      if (!savePath.endsWith(".pwsseq")) {
         savePath = savePath + ".pwsseq"; //Make sure the extension is there.
      }
      try (FileWriter writer = new FileWriter(
            savePath)) { //Writer is automatically closed at the end of this statement.
         Gson gson = GsonUtils.getGson();
         String json = gson.toJson(this);
         writer.write(json);
      }
   }
}

class StepTypeAdapter extends TypeAdapter<Step> {

   //This custom adapter enables Steps to be Jsonified by GSON even though they have a circular parent/child reference.
   public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
      @Override
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
         if (Step.class.isAssignableFrom(type.getRawType())) { //Allow subtypes to use this factory.
            return (TypeAdapter<T>) new StepTypeAdapter(gson);
         }
         return null;
      }
   };

   private final Gson gson;

   private StepTypeAdapter(Gson gson) {
      this.gson = gson;
   }

   @Override
   public void write(JsonWriter out, Step step) throws IOException {
      out.beginObject();
      out.name("id");
      out.value(step.getID());
      out.name("stepType");
      out.value(step.getType());
      out.name("settings");
      gson.toJson(step.getSettings(), SequencerConsts.getFactory(step.getType()).getSettings(),
            out);
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
         Step step = (Step) SequencerConsts.getFactory(stepType).getStep().newInstance();
         if (!in.nextName().equals("settings")) {
            throw new IOException("Json Parse Error");
         }
         JsonableParam settings = gson
               .fromJson(in, SequencerConsts.getFactory(stepType).getSettings());
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
      } catch (InstantiationException | IllegalAccessException e) {
         throw new RuntimeException(e);
      } catch (IOException | JsonIOException | IllegalStateException ioe) {
         try {
            while (in.hasNext()) {
               in.skipValue();
            } //Read out the rest of the failed json object before returning.
            in.endObject();
            //Rather than allow an exception to break the whole loading process just insert the
            // special "Broken step" where the error occurred.
            return (Step) SequencerConsts.getFactory(SequencerConsts.Type.BROKEN.name()).getStep()
                  .newInstance();
         } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
         }
      }
   }
}