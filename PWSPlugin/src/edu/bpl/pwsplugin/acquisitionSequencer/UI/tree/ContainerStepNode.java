/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author nick
 */
public class ContainerStepNode extends StepNode {
    public ContainerStepNode(ContainerStepNode node) { //copy constructor
        super(node);
    }
    
    
    public ContainerStepNode(JsonableParam settings, Consts.Type type) {
        super(settings, type);
        this.setAllowsChildren(true);
        if (!Consts.isContainer(type)) {
            throw new RuntimeException("Creating ContainerStepNode for a non-container step type.");
        }
    }
}
