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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.micromanager.internal.utils.FileDialogs;

/**
 *
 * @author nick
 */
public abstract class Step extends CopyableMutableTreeNode {
    private JsonableParam settings; 
    private final Consts.Type stepType;
    protected final List<SequencerFunction> callbacks = new ArrayList<>();

    
    public Step(JsonableParam settings, Consts.Type type) {
        super();
        this.stepType = type;
        this.setSettings(settings);
    }
    
    public Step(Step step) { //copy constructor
        this(step.settings.copy(), step.stepType);        
    }
    
    public final Consts.Type getType() {
        return stepType;
    }

    public final JsonableParam getSettings() { return (JsonableParam) settings.copy(); }
    
    public final void setSettings(JsonableParam settings) { this.settings = settings; }
    
    protected abstract SequencerFunction stepFunc();
    
    public static final FileDialogs.FileType FILETYPE = new FileDialogs.FileType("PWS Acquisition Sequence", "Sequence (.pwsseq)", "newAcqSequence.pwsseq", true, "pwsseq");
    
    public abstract Double numberNewAcqs(); //Return the number of new cell folders expected to be created within this step.
    
    public final SequencerFunction getFunction() {
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                for (SequencerFunction func : callbacks) {
                    status = func.apply(status);
                } 
                return stepFunc().apply(status);
            }
        };
    }
    
    public final void addCallback(SequencerFunction cb) { 
        callbacks.add(cb);
    }
    
    public abstract List<String> requiredRelativePaths(Integer startingCellNum);
    
    @Override
    public String toString() { //this determines how its labeled in a JTree
        return Consts.getFactory(this.getType()).getName();
    }
    
    public static void registerGsonType() {
        GsonUtils.registerType(StepTypeAdapter.FACTORY);
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