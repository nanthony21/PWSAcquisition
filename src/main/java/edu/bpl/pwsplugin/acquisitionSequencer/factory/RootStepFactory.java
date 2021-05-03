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

package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.RootStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class RootStepFactory extends StepFactory {

   //Should only exist once as the root of each experiment, sets the needed root parameters.
   @Override
   public Class<? extends BuilderJPanel> getUI() {
      return RootStepUI.class;
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return SequencerSettings.RootStepSettings.class;
   }

   @Override
   public Class<? extends Step> getStep() {
      return RootStep.class;
   }

   @Override
   public String getDescription() {
      return "Initial settings for the experiment.";
   }

   @Override
   public String getName() {
      return "Initialization";
   }

   @Override
   public String getCategory() {
      throw new UnsupportedOperationException();
   }

   @Override
   public SequencerConsts.Type getType() {
      return SequencerConsts.Type.ROOT;
   }
}

class RootStepUI extends BuilderJPanel<SequencerSettings.RootStepSettings> {

   private final String TEMPLATEDESCRIPTION =
         "Experiment Description: ?\n\nTemperature: ?C    Humidity: ?%\n\nIncubator Status: ?\n\nEstimated Confluency: ?%\n\nIncubationPeriod: ?hrs\n\n"
               + "Where to find project information: ?\n\nOil Type: Type 37";
   DirectorySelector directory = new DirectorySelector(
         DirectorySelector.DefaultMMFunctions.MMDataSetDirectory);
   JTextArea description = new JTextArea(TEMPLATEDESCRIPTION);
   JTextField author = new JTextField("");
   JTextField project = new JTextField("");
   JTextField cellLine = new JTextField("");
   JButton defaultButton = new JButton("Reset Description");

   public RootStepUI() {
      super(new MigLayout("insets 0 0 0 0, nogrid"), SequencerSettings.RootStepSettings.class);

      JScrollPane scroll = new JScrollPane(description);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      super.add(new JLabel("Folder:"));
      super.add(directory, "wrap, growx, spanx");
      super.add(new JLabel("Author:"));
      super.add(author, "wrap, pushx, growx");
      super.add(new JLabel("Project:"));
      super.add(project, "wrap, pushx, growx");
      super.add(new JLabel("Cell Line:"));
      super.add(cellLine, "wrap, pushx, growx");
      super.add(new JLabel("Description:"), "wrap, spanx");
      super.add(scroll, "grow, push, spanx, wrap");
      super.add(defaultButton);

      description.setEditable(true);
      description.setLineWrap(true);
      description.setWrapStyleWord(true);
      description.setBorder(BorderFactory.createLoweredSoftBevelBorder());

      defaultButton.addActionListener((evt) -> {
         this.description.setText(this.TEMPLATEDESCRIPTION);
      });

   }

   @Override
   public void populateFields(SequencerSettings.RootStepSettings settings) {
      directory.setText(settings.directory);
      if (!settings.description
            .equals("")) { //No point populating an empty string, just leave the default.
         description.setText(settings.description);
      }
      project.setText(settings.project);
      author.setText(settings.author);
      cellLine.setText(settings.cellLine);
   }

   @Override
   public SequencerSettings.RootStepSettings build() {
      SequencerSettings.RootStepSettings settings = new SequencerSettings.RootStepSettings();
      settings.directory = this.directory.getText();
      settings.description = description.getText();
      settings.project = project.getText();
      settings.author = author.getText();
      settings.cellLine = cellLine.getText();
      return settings;
   }
}