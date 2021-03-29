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

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.List;

/**
 *
 * @author nick
 */
public class BrokenStep extends ContainerStep<JsonableParam> {
    public BrokenStep() {
        super(new JsonableParam(), SequencerConsts.Type.BROKEN.name());
    }
    
    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
        return (status) -> { throw new RuntimeException("The BrokenStep should never be run."); };
    }
    
    @Override
    public SimFn getSimulatedFunction() {
        return (status) -> { throw new RuntimeException("The BrokenStep should never be run."); };
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = super.validate();
        errs.add("The BROKEN step is caused by an error and can not be run. It must be replaced.");
        return errs;
    }   
}