/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.tree;

import edu.bpl.pwsplugin.UI.sequencer.Consts;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author nick
 */
public class StepNode extends CopyableMutableTreeNode {
    Consts.Type type;
    JsonableParam settings;
    public StepNode(JsonableParam settings, Consts.Type type) {
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
    
    public JsonableParam getSettings() {
        return this.settings;
    }
    
    public void setSettings(JsonableParam settings) {
        this.settings = settings;
    }
    
    @Override
    protected void copyAttributes(CopyableMutableTreeNode from, CopyableMutableTreeNode to) {
        if (!(from instanceof StepNode) || !(to instanceof StepNode)) {
            throw new RuntimeException("Type error");
        }
        super.copyAttributes(from, to);
        ((StepNode)to).type = ((StepNode)from).type;
        ((StepNode)to).settings = ((StepNode)from).settings.copy();
    }
    
    @Override
    public String toString() {
        return Consts.getName(type);
    }
}
