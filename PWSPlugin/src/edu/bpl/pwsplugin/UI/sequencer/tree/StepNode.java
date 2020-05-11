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
    public StepNode(JsonableParam settings, Consts.Type type) {
        super();     
        this.type = type;
        this.setUserObject(settings);
    }
    
    public StepNode() {
        super();
    }
    
    public Consts.Type getType() {
        return type;
    }
    
    @Override
    protected void copyAttributes(CopyableMutableTreeNode from, CopyableMutableTreeNode to) {
        if (!(from instanceof StepNode) || !(to instanceof StepNode)) {
            throw new RuntimeException("Type error");
        }
        ((StepNode)to).type = ((StepNode)from).type;
    }
    
    @Override
    public String toString() {
        return Consts.getName(type);
    }
}
