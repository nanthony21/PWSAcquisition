/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyableMutableTreeNode;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
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
    
    public Step(Consts.Type type) {
        super();
        stepType = type;
    }
    
    public Step(JsonableParam settings, Consts.Type type) {
        this(type);
        this.setSettings(settings);
    }
    
    public Step(Step step) { //copy constructor
        this(step.stepType);
        setSettings(step.settings.copy());
        
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
    
}
