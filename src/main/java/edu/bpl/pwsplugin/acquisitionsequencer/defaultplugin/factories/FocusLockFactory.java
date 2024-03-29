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

package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps.FocusLock;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class FocusLockFactory implements StepFactory {

   @Override
   public BuilderJPanel<?> createUI() {
      return new FocusLockUI();
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return FocusLockFactory.FocusLockSettings.class;
   }

   @Override
   public Step<?> createStep() {
      return new FocusLock();
   }

   @Override
   public String getDescription() {
      return "Engage continuous hardware autofocus. Focus lock will be checked before execution of each Acquisition within this.";
   }

   @Override
   public String getName() {
      return "Optical Focus Lock";
   }

   @Override
   public String getCategory() {
      return "Focus";
   }

   public static class FocusLockSettings extends JsonableParam {
      public double delay = 1; //Seconds delay after focus
   }
}

class FocusLockUI extends SingleBuilderJPanel<FocusLockFactory.FocusLockSettings> {

   ImprovedComponents.Spinner delay;

   public FocusLockUI() {
      super(new MigLayout(), FocusLockFactory.FocusLockSettings.class);

      delay = new ImprovedComponents.Spinner(new SpinnerNumberModel(1.0, 0.0, 30.0, 1.0));
      ((ImprovedComponents.Spinner.DefaultEditor) delay.getEditor()).getTextField().setColumns(4);

      this.add(new JLabel("Delay (s):"), "gapleft push");
      this.add(delay);
   }

   @Override
   public Map<String, Object> getPropertyFieldMap() {
      Map<String, Object> m = new HashMap<>();
      m.put("delay", delay);
      return m;
   }
}

