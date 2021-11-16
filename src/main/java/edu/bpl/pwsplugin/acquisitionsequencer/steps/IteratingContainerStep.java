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

import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 * A base class for ContainerSteps that run their child steps multiple times.
 * @author N2-LiveCell
 */
public abstract class IteratingContainerStep<T extends JsonableParam> extends ContainerStep<T> {

   public IteratingContainerStep(T settings, String type) {
      super(settings, type);
   }

   /**
    * Required copy constructor
    * @param step
    */
   public IteratingContainerStep(IteratingContainerStep<T> step) {
      super(step);
   }

   /**
    *
    * @return The number of times this step plans to iterate.
    */
   public abstract Integer getTotalIterations();

   /**
    *
    * @return The current iteration we are on. Starts at 0.
    */
   public abstract Integer getCurrentIteration();

}
