///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreeNode;

/**
 *
 * @author nick
 */
public abstract class ContainerStep<T extends JsonableParam> extends Step<T> {
    //A `Step` that takes other `Step`s and wraps functionality around them.
    
    public ContainerStep(T settings, String type) {
        super(settings, type);
    }
    
    public ContainerStep(ContainerStep step) {
        super(step);
    }    
    
    public final List<Step> getSubSteps() {
        return (List<Step>) (List<? extends TreeNode>) Collections.list(this.children());
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        if (this.getSubSteps().isEmpty()) {
            errs.add(String.format("%s container-node may not be empty", this.toString()));
        }
        return errs;
    }
    
    protected final SequencerFunction getSubstepsFunction(List<SequencerFunction> callbacks) { // Execute each substep in sequence
        List<SequencerFunction> stepFunctions = new ArrayList<>();
        for (Step substep : this.getSubSteps()) { //Pass callbacks on to child steps.
            stepFunctions.add(substep.getFunction(callbacks));
        }
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                for (SequencerFunction func : stepFunctions) {
                    status = func.apply(status);
                }
                return status;   
            }
        };
    }
    
    protected SimFn getSubStepSimFunction() {
        List<SimFn> stepFunctions = this.getSubSteps().stream().map(Step::getSimulatedFunction).collect(Collectors.toList());
        return (status) -> {
            for (SimFn fn : stepFunctions) {
                status = fn.apply(status);
            }
            return status;
        };
    }
}
