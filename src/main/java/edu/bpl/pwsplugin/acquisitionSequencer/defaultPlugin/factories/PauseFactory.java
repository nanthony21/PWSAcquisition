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
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps.PauseStep;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class PauseFactory extends StepFactory {

   @Override
   public BuilderJPanel<?> createUI() {
      return new PauseStepUI();
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return SequencerSettings.PauseStepSettings.class;
   }

   @Override
   public Step<?> createStep() {
      return new PauseStep();
   }

   @Override
   public String getDescription() {
      return "Open a dialog window and pause execution until the dialog is closed.";
   }

   @Override
   public String getName() {
      return "Pause";
   }

   @Override
   public String getCategory() {
      return "Utility";
   }
}

class PauseStepUI extends BuilderJPanel<SequencerSettings.PauseStepSettings> {

   JTextArea message = new JTextArea();

   public PauseStepUI() {
      super(new MigLayout("insets 0 0 0 0, fill"), SequencerSettings.PauseStepSettings.class);

      //message.setPreferredSize(new Dimension(100, 100));
      message.setBorder(BorderFactory.createLoweredBevelBorder());
      message.setLineWrap(true);
      message.setWrapStyleWord(true);

      JScrollPane scroll = new JScrollPane(message);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      this.add(new JLabel("Message:"), "wrap");
      this.add(scroll,
            "w 100%, h 100%"); //This does the same as "grow" except that grow wasn't working for some reason here.
   }

   @Override
   public SequencerSettings.PauseStepSettings build() {
      SequencerSettings.PauseStepSettings settings = new SequencerSettings.PauseStepSettings();
      settings.message = message.getText();
      return settings;
   }

   @Override
   public void populateFields(SequencerSettings.PauseStepSettings settings) {
      this.message.setText(settings.message);
   }
}
