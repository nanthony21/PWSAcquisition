/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import edu.bpl.pwsplugin.acquisitionSequencer.UI.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public abstract class StepNode extends CopyableMutableTreeNode {
    Consts.Type type;
    SequencerSettings settings;
    
    public StepNode(SequencerSettings settings, Consts.Type type) {
        this();     
        this.type = type;
        this.settings = settings;
    }
    
    protected StepNode() {
        super();
    }
    
    public Consts.Type getType() {
        return type;
    }
    
    public Step createStepObject() throws InstantiationException, IllegalAccessException {
        Step obj = Consts.getStepObject(this.getType()).newInstance();
        obj.setSettings(this.getSettings());
        return obj;
    }
    
    public SequencerSettings getSettings() {
        return (SequencerSettings) this.settings.copy();
    }
    
    public void setSettings(SequencerSettings settings) {
        if (settings == null) {
            throw new RuntimeException("Setting null settings to step node");
        }
        this.settings = settings;
    }
    
    @Override
    protected void copyAttributesFrom(DefaultMutableTreeNode from) {
        if (!(from instanceof StepNode)) {
            throw new RuntimeException("Type error");
        }
        super.copyAttributesFrom(from);
        this.type = ((StepNode)from).type;
        this.settings = ((SequencerSettings)((StepNode)from).settings.copy());
    }
    
    @Override
    public String toString() {
        return Consts.getName(type);
    }
}
