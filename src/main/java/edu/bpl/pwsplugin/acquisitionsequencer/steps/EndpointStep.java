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
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public abstract class EndpointStep<T extends JsonableParam> extends Step<T> {

   //A `Step` which is an endpoint (does not support containing any substeps
   public EndpointStep(T settings, String type) {
      super(settings, type);
      this.setAllowsChildren(false);
   }

   public EndpointStep(EndpointStep step) {
      super(step);
      this.setAllowsChildren(false);
   }
}
