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
public class EndpointStepNode extends StepNode {
    public EndpointStepNode() {
        super();
        this.setAllowsChildren(false);
    }
    
    public EndpointStepNode(JsonableParam settings, Consts.Type type) {
        super(settings, type);
        this.setAllowsChildren(false);
    }
    
}
