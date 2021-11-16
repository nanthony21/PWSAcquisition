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

package edu.bpl.pwsplugin.acquisitionsequencer.steps;

import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreeNode;

/**
 * A `Step` that takes other `Step`s and wraps functionality around them.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public abstract class ContainerStep<T extends JsonableParam> extends Step<T> {
   public ContainerStep(T settings, String type) {
      super(settings, type);
   }

   public ContainerStep(ContainerStep<T> step) {
      super(step);
   }

   /**
    *
    * @return A list of the all of the steps that are direct children of this step. May be EndpointSteps or more ContainerSteps
    */
   public final List<Step<?>> getSubSteps() {
      return (List<Step<?>>) (List<? extends TreeNode>) Collections.list(this.children());
   }

   /**
    * Make sure that the container step is not empty. This is not valid.
    * @return A list of error strings.
    */
   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      if (this.getSubSteps().isEmpty()) {
         errs.add(String.format("%s container-node may not be empty", this.toString()));
      }
      return errs;
   }

   /**
    * Return a function that will execute each substep function in sequence
    * @param callbacks Callbacks passed from above that will be executed before each step function.
    * @return A function that executes the child functions.
    */
   protected final SequencerFunction getSubstepsFunction(List<SequencerFunction> callbacks) {
      List<SequencerFunction> stepFunctions = new ArrayList<>();
      for (Step<?> substep : getSubSteps()) { //Pass callbacks on to child steps.
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

   /**
    *
    * @return A function that will call all substep simulation functions.
    */
   protected SimFn getSubStepSimFunction() {
      List<SimFn> stepFunctions = this.getSubSteps().stream().map(Step::getSimulatedFunction)
            .collect(Collectors.toList());
      return (status) -> {
         for (SimFn fn : stepFunctions) {
            status = fn.apply(status);
         }
         return status;
      };
   }
}
