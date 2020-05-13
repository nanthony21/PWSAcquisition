/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import edu.bpl.pwsplugin.acquisitionSequencer.UI.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
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
    
    public SequencerSettings getSettings() {
        return this.settings;
    }
    
    public void setSettings(SequencerSettings settings) {
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
