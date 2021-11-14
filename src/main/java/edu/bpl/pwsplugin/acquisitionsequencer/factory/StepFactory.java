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
public abstract class StepFactory {

   //A factory to provide access to a `Step` class, a JsonableParam settings class, and a UI JPanel to adjust the settings. The `Step` must have a no-args constructor to work with the GSON type adapter.
   public abstract BuilderJPanel<?> createUI();

   public abstract Class<? extends JsonableParam> getSettings();

   public abstract Step<?> createStep();

   public abstract String getDescription();

   public abstract String getName();

   public abstract String getCategory();

   public void registerGson() {
      JsonableParam.registerClass(getSettings());
   }
}
