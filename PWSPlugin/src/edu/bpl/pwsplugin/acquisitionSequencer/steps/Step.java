/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyableMutableTreeNode;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.micromanager.internal.utils.FileDialogs;

/**
 *
 * @author nick
 */
public abstract class Step<T extends JsonableParam> extends CopyableMutableTreeNode {
    protected T settings; 
    private final Consts.Type stepType;
    protected final List<SequencerFunction> callbacks = new ArrayList<>();

    
    public Step(T settings, Consts.Type type) {
        super();
        this.stepType = type;
        this.setSettings(settings);
    }
    
    public Step(Step step) { //copy constructor
        this((T) step.settings.copy(), step.stepType);        
    }
    
    public final Consts.Type getType() {
        return stepType;
    }

    @Override
    public Step copy() { //Use json to safely copy the object
        Gson gson = GsonUtils.getGson();
        return (Step) gson.fromJson(gson.toJson(this), this.getClass());
    }
    
    public final T getSettings() { return (T) settings.copy(); }
    
    public final void setSettings(T settings) { this.settings = settings; }
    
    protected abstract SequencerFunction getStepFunction(); //Return  function to run for this step during execution. Does not include callbacks and mandatory changes to the status object which are handled automatically by `getFunction`.
          
    protected abstract void initializeSimulatedRun(); //Some steps need to keep track of context to properly do simulateRun. this reinitializes the context.
    
    protected abstract SimulatedStatus simulateRun(SimulatedStatus status); //return the filePaths that will be used by running this step.
    
    protected class SimulatedStatus {
        public Integer cellNum = 1;
        public List<String> requiredPaths = new ArrayList<>();
        public String workingDirectory = "";
    }
    
    public final SequencerFunction getFunction() {
        TreeNode[] path = this.getPath();
        Step[] treePath = Arrays.copyOf(path, path.length, Step[].class); //cast to Step[].
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                //Update the status object with information about the current step.
                status.setTreePath(treePath);
                //Run any callbacks that have been set for this step.
                for (SequencerFunction func : callbacks) {
                    status = func.apply(status);
                } 
                //Run the function for this step
                return getStepFunction().apply(status);
            }
        };
    }
    
    public final void addCallback(SequencerFunction cb) { 
        callbacks.add(cb);
    }
        
    @Override
    public String toString() { //this determines how its labeled in a JTree
        return Consts.getFactory(this.getType()).getName();
    }
    
    public abstract List<String> validate(); //Return a list of any errors for this step.
    
    public static void registerGsonType() { //This must be called for GSON loading/saving to work.
        GsonUtils.registerType(StepTypeAdapter.FACTORY);
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
        out.name("stepType");
        out.value(step.getType().name());
        out.name("settings");
        gson.toJson(step.getSettings(), Consts.getFactory(step.getType()).getSettings(), out);
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
            if (!in.nextName().equals("stepType")) { throw new RuntimeException(); } //This must be "stepType" 
            Consts.Type stepType = Consts.Type.valueOf(in.nextString());
            Step step = Consts.getFactory(stepType).getStep().newInstance();
            if (!in.nextName().equals("settings")) { throw new RuntimeException(); }
            JsonableParam settings = gson.fromJson(in, Consts.getFactory(stepType).getSettings());
            step.setSettings(settings);
            if (step.getAllowsChildren()) {
                if (!in.nextName().equals("children")) { throw new RuntimeException(); }
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
        }
    }
}