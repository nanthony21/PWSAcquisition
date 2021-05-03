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
import edu.bpl.pwsplugin.acquisitionsequencer.steps.BrokenStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

/**
 * @author nick
 */
public class BrokenStepFactory extends StepFactory {

   //A step only used for a placeholder when we unsucessfully try to load a step.
   @Override
   public Class<? extends BuilderJPanel> getUI() {
      return BrokenStepUI.class;
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return JsonableParam.class;
   }

   @Override
   public Class<? extends Step> getStep() {
      return BrokenStep.class;
   }

   @Override
   public String getDescription() {
      return "This step failed to load and needs to be replaced.";
   }

   @Override
   public String getName() {
      return "BROKEN";
   }

   @Override
   public String getCategory() {
      throw new UnsupportedOperationException();
   }

   @Override
   public SequencerConsts.Type getType() {
      return SequencerConsts.Type.BROKEN;
   }
}

class BrokenStepUI extends BuilderJPanel<JsonableParam> {

   public BrokenStepUI() {
      super(new MigLayout("insets 0 0 0 0"), JsonableParam.class);

      this.add(new JLabel("LOADING FAILED!!!"));
   }

   @Override
   public JsonableParam build() {
      return new JsonableParam();
   }

   @Override
   public void populateFields(JsonableParam settings) {
   }
}

