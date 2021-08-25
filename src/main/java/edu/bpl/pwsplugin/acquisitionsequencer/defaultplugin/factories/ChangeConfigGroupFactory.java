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

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps.ChangeConfigGroup;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ChangeConfigGroupFactory extends StepFactory {

   @Override
   public BuilderJPanel<?> createUI() {
      return new ChangeConfigGroupUI();
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return SequencerSettings.ChangeConfigGroupSettings.class;
   }

   @Override
   public Step<?> createStep() {
      return new ChangeConfigGroup();
   }

   @Override
   public String getDescription() {
      return "Change one of the Micro-Manager configuration groups and then optionally "
            + "change back to the original setting at the end."
            + " This could be used to change the objective, fluorescence filter, etc.";
   }

   @Override
   public String getName() {
      return "Configuration Group";
   }

   @Override
   public String getCategory() {
      return "Utility";
   }
}

class ChangeConfigGroupUI extends
      BuilderJPanel<SequencerSettings.ChangeConfigGroupSettings> implements ItemListener {

   JComboBox<String> configGroupName = new JComboBox<>();
   JComboBox<String> configValue = new JComboBox<>();
   JCheckBox resetWhenFinished = new JCheckBox("Recover original setting when finished.");

   public ChangeConfigGroupUI() {
      super(new MigLayout("fillx"), SequencerSettings.ChangeConfigGroupSettings.class);

      configGroupName.addItemListener(this);

      resetWhenFinished.setHorizontalTextPosition(SwingConstants.LEFT);

      updateConfigGroupComboBox();

      this.add(new JLabel("Group Name:"), "gapleft push");
      this.add(configGroupName, "wrap, al right, growx");
      this.add(new JLabel("Setting:"), "gapleft push");
      this.add(configValue, "wrap, al right, growx");
      this.add(resetWhenFinished, "span, gapleft push");
   }

   private void updateConfigGroupComboBox() {
      String[] s = Globals.core().getAvailableConfigGroups().toArray();
      configGroupName.setModel(new DefaultComboBoxModel<>(s));
   }

   @Override
   public void populateFields(SequencerSettings.ChangeConfigGroupSettings settings) {
      this.configGroupName.setSelectedItem(settings.configGroupName);
      this.configValue.setSelectedItem(settings.configValue);
      this.resetWhenFinished.setSelected(settings.resetWhenFinished);
   }

   @Override
   public SequencerSettings.ChangeConfigGroupSettings build() {
      SequencerSettings.ChangeConfigGroupSettings settings =
            new SequencerSettings.ChangeConfigGroupSettings();
      settings.configGroupName = (String) this.configGroupName.getSelectedItem();
      settings.configValue = (String) this.configValue.getSelectedItem();
      settings.resetWhenFinished = this.resetWhenFinished.isSelected();
      return settings;
   }

   @Override
   public void itemStateChanged(
         ItemEvent evt) { //Fired with the value of the config group changes, update the available values to match the config group
      String[] s = Globals.core().getAvailableConfigs((String) evt.getItem()).toArray();
      configValue.setModel(new DefaultComboBoxModel<>(s));
   }
}

