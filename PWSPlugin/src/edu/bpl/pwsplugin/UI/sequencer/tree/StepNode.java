/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.tree;

import edu.bpl.pwsplugin.UI.sequencer.Consts;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public class StepNode extends DefaultMutableTreeNode {
    Consts.Type type;
    public StepNode(JsonableParam settings, Consts.Type type) {
        super();     
        this.type = type;
        this.setUserObject(settings);
    }
    
    public Consts.Type getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return Consts.getName(type);
    }
}
