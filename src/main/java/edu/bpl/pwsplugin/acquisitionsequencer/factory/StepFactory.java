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

package edu.bpl.pwsplugin.acquisitionsequencer.factory;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 * Responsible for generating instances of a `Step`, the settings for the `Step` and a UI to set
 * the settings graphically.
 *
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public interface StepFactory {

   /**
    *
    * @return A User interface for generating/displaying settings for the `Step`
    */
   BuilderJPanel<?> createUI();

   /**
    *
    * @return The class that acts as a container for settings to initialize the `Step`
    */
   Class<? extends JsonableParam> getSettings();

   /**
    *
    * @return A new instance of the `Step`
    */
   Step<?> createStep();

   /**
    *
    * @return A text description of what the `Step` does.
    */
   String getDescription();

   /**
    *
    * @return A name ot refer to the `Step` by.
    */
   String getName();

   String getCategory();
}
