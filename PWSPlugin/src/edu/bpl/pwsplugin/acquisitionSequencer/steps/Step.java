/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyableMutableTreeNode;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.swing.tree.TreeNode;

/**
 *
 * @author nick
 */
public abstract class Step<T extends JsonableParam> extends CopyableMutableTreeNode {
    protected T settings; 
    private final SequencerConsts.Type stepType;
    protected final List<SequencerFunction> callbacks = new ArrayList<>();
    private static final AtomicInteger counter = new AtomicInteger(); //This static counter makes sure that each Step object has it's own uid during runtime.
    private Integer uid = counter.getAndIncrement();

    
    public Step(T settings, SequencerConsts.Type type) {
        super();
        this.stepType = type;
        this.setSettings(settings);
    }
    
    
    public Step(Step step) { //copy constructor
        this((T) step.settings.copy(), step.stepType);        
    }
    
    public Integer getID() { return this.uid; }
        
    public final SequencerConsts.Type getType() {
        return stepType;
    }

    @Override
    public Step copy() { //Use json to safely copy the object
        Gson gson = GsonUtils.getGson();
        return (Step) gson.fromJson(gson.toJson(this), this.getClass());
    }
    
    public final T getSettings() { return (T) settings.copy(); }
    
    public final void setSettings(T settings) { this.settings = settings; }
    
    protected abstract SequencerFunction getStepFunction(); //Return  function to run for this step during execution. Does not include callbacks and mandatory changes to the status object which are handled automatically by `getFunction`. This should initialize any variables that are used for context during runtime.
          
    protected abstract SimFn getSimulatedFunction(); //return a function that simulates how folder usage and cell number changes through the run.
    
    protected static class SimulatedStatus {
        public Integer cellNum = 1;
        public List<String> requiredPaths = new ArrayList<>();
        public String workingDirectory = "";
    }
    
    @FunctionalInterface
    protected static interface SimFn extends Function<SimulatedStatus, SimulatedStatus> {} 
    
    public final SequencerFunction getFunction() {
        SequencerFunction stepFunc = this.getStepFunction();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                //Update the status object with information about the current step.
                status.coords().moveDownTree(Step.this); //Append this step to the end of our coordinate path.
                //Run any callbacks that have been set for this step.
                for (SequencerFunction func : callbacks) {
                    status = func.apply(status);
                } 
                //Run the function for this step subclass.
                status = stepFunc.apply(status);
                status.coords().moveUpTree(); //Set the path back to where it was as we exit this step
                return status;
            }
        };
    }
    
    public final void addCallback(SequencerFunction cb) { 
        callbacks.add(cb);
    }
        
    @Override
    public String toString() { //this determines how its labeled in a JTree
        return SequencerConsts.getFactory(this.getType()).getName();
    }
    
    public abstract List<String> validate(); //Return a list of any errors for this step.
    
    public static void registerGsonType() { //This must be called for GSON loading/saving to work.
        GsonUtils.builder().registerTypeAdapterFactory(StepTypeAdapter.FACTORY);
    }
    
    public void saveToJson(String savePath) throws IOException {
        if(!savePath.endsWith(".pwsseq")) {
            savePath = savePath + ".pwsseq"; //Make sure the extension is there.
        }
        try (FileWriter writer = new FileWriter(savePath)) { //Writer is automatically closed at the end of this statement.
            Gson gson = GsonUtils.getGson();
            String json = gson.toJson(this);
            writer.write(json);
        }
    }
}

class StepTypeAdapter extends TypeAdapter<Step> {
    //This custom adapter enables Steps to be Jsonified even though they have a circular parent/child reference.
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
        out.value(step.getType().name());
        out.name("settings");
        gson.toJson(step.getSettings(), SequencerConsts.getFactory(step.getType()).getSettings(), out);
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
            if (!in.nextName().equals("id")) { throw new IOException("Json Parse Error"); } //ID is determined at runtime don't load it.
            int id = in.nextInt(); //read the id to get rid of it.
            if (!in.nextName().equals("stepType")) { throw new IOException("Json Parse Error"); } //This must be "stepType" 
            SequencerConsts.Type stepType = SequencerConsts.Type.valueOf(in.nextString());
            Step step = SequencerConsts.getFactory(stepType).getStep().newInstance();
            if (!in.nextName().equals("settings")) { throw new IOException("Json Parse Error"); }
            JsonableParam settings = gson.fromJson(in, SequencerConsts.getFactory(stepType).getSettings());
            step.setSettings(settings);
            if (step.getAllowsChildren()) {
                if (!in.nextName().equals("children")) { throw new IOException("Json Parse Error"); }
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
                return SequencerConsts.getFactory(SequencerConsts.Type.BROKEN).getStep().newInstance();//Rather than allow an exception to break the whole loading process just insert the special "Broken step" where the error occured.
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}