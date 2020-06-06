/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public abstract class StepNode extends CopyableMutableTreeNode {
    
    public StepNode(JsonableParam settings, Consts.Type type) {
        this();     
        this.type = type;
        this.settings = settings;
    }
    
    protected StepNode() {
        super();
        Step obj = Consts.getFactory(type).createStep();
        obj.setSettings(settings);
        this.setUserObject(obj);
    }
    
    public Consts.Type getType() {
        return ((Step) this.getUserObject()).getType();
    }
    
    public JsonableParam getSettings() {
        return this.createStepObject().getSettings();
    }
    
    public void setSettings(JsonableParam settings) {
        if (settings == null) {
            throw new RuntimeException("Setting null settings to step node");
        }
        this.createStepObject().setSettings(settings);
    }
    
    public Step createStepObject() {
        return (Step) this.getUserObject();
    }
    
    @Override
    protected void copyAttributesFrom(DefaultMutableTreeNode from) {
        if (!(from instanceof StepNode)) {
            throw new RuntimeException("Type error");
        }
        super.copyAttributesFrom(from);
    }
    
    @Override
    public String toString() { //this selects how its labeled in a JTree
        return Consts.getFactory(this.getType()).getName();
    }
}
