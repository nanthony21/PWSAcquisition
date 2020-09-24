/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author N2-LiveCell
 */
public abstract class IteratingContainerStep<T extends JsonableParam> extends ContainerStep<T> {
    //A container step that runs it's substeps multiple times.
    
    public IteratingContainerStep(T settings, SequencerConsts.Type type) {
        super(settings, type);
    }
    
    public IteratingContainerStep(IteratingContainerStep step) {
        super(step); //Required copy constructor
    }
    
    public abstract Integer getTotalIterations();
    
    public abstract Integer getCurrentIteration();
    
}
