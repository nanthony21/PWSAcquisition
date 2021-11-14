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

package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.CheckBoxPanel;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.hardware.configurations.HWConfiguration;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSSettingsConsts;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AcquireCellUI extends BuilderJPanel<AcquireCellSettings> {

   AdvancedAcquireCellUI advancedUI = new AdvancedAcquireCellUI();
   SimpleAcquireCellUI simpleUI = new SimpleAcquireCellUI(advancedUI);
   BuilderJPanel<AcquireCellSettings> currentTab;
   JTabbedPane tabs = new JTabbedPane();

   public AcquireCellUI() {
      super(new MigLayout("insets 0 0 0 0, fill"), AcquireCellSettings.class);

      ((MigLayout) advancedUI.getLayout()).setLayoutConstraints(
            "insets 5"); //Override the default inset value of 0 for aesthetic purposes.

      tabs.add("Simple", simpleUI);
      tabs.add("Advanced", advancedUI);
      currentTab = (BuilderJPanel<AcquireCellSettings>) tabs.getSelectedComponent();

      tabs.addChangeListener(
            (evt) -> { //When tab is switched populate the new tab with the settings from the old tab.
               BuilderJPanel newTab = (BuilderJPanel) tabs.getSelectedComponent();
               try {
                  newTab.populateFields(currentTab.build());
               } catch (Exception e) {
                  Globals.mm().logs().showError(e);
               }
               currentTab = newTab;
            });

      this.add(tabs, "shrinkx");
   }

   @Override
   public void populateFields(AcquireCellSettings settings) throws BuilderPanelException {
      advancedUI.populateFields(settings);
      simpleUI.populateFields(settings);
   }

   @Override
   public AcquireCellSettings build() throws BuilderPanelException {
      BuilderJPanel<AcquireCellSettings> panel = (BuilderJPanel) tabs.getSelectedComponent();
      return panel.build();
   }
}


class SimpleAcquireCellUI extends BuilderJPanel<AcquireCellSettings> implements
      PropertyChangeListener {

   //Provides a simplified view of the settings in the AdvancedAcquireCellUI
   private final SimplePWSPanel pwsSettings = new SimplePWSPanel();
   private final SimplePWSPanel dynSettings = new SimplePWSPanel();
   private final CheckBoxPanel pwsCBPanel = new CheckBoxPanel(pwsSettings, "PWS");
   private final CheckBoxPanel dynCBPanel = new CheckBoxPanel(dynSettings, "Dynamics");
   private final JButton systemDefault = new JButton("Use Defaults");
   private final ListCardUI<List<FluorSettings>, FluorSettings> fluorSettings = new ListCardUI(
         ArrayList.class, "", new FluorSettings());
   private final CheckBoxPanel fluorCBPanel = new CheckBoxPanel(fluorSettings, "Fluorescence");
   private final AdvancedAcquireCellUI advancedUI;
         // This reference to an `advancedUI` is used to get the values for settings that aren't displayed in this ui.


   public SimpleAcquireCellUI(AdvancedAcquireCellUI advanceUI) {
      super(new MigLayout(), AcquireCellSettings.class);

      this.advancedUI = advanceUI;

      this.systemDefault.addActionListener((evt) -> { //Apply default settings
         JPopupMenu menu = new JPopupMenu();
         for (PWSSettingsConsts.Systems sys : PWSSettingsConsts.Systems.values()) {
            JMenuItem item = new JMenuItem(sys.name());
            item.addActionListener((evnt) -> {
               try {
                  this.populateFields(AcquireCellSettings.getDefaultSettings(sys));
               } catch (Exception e) {
                  Globals.mm().logs().logError(e);
               }
            });
            menu.add(item);
         }
         menu.show(systemDefault, 0, 0);
      });

      pwsSettings.setBorder(BorderFactory.createEtchedBorder());
      dynSettings.setBorder(BorderFactory.createEtchedBorder());
      fluorSettings.setBorder(BorderFactory.createEtchedBorder());

      super.add(pwsCBPanel, "wrap, spanx");
      super.add(dynCBPanel, "wrap, spanx");
      super.add(fluorCBPanel, "spanx");
      super.add(systemDefault, "wrap");

      //We can't yet reference Globals on initialization. at least initialize an empty state. the property change listener should get fired afterward.
      Globals.addPropertyChangeListener(this);
      this.setConfigNames(new ArrayList());
   }

   @Override
   public void populateFields(AcquireCellSettings settings) throws BuilderPanelException {
      pwsCBPanel.setSelected(settings.pwsEnabled);
      pwsSettings.setExposure(settings.pwsSettings.exposure);

      dynCBPanel.setSelected(settings.dynEnabled);
      dynSettings.setExposure(settings.dynSettings.exposure);

      fluorCBPanel.setSelected(settings.fluorEnabled);
      fluorSettings.populateFields(settings.fluorSettings);

      this.advancedUI.populateFields(settings);
   }

   @Override
   public AcquireCellSettings build() throws BuilderPanelException {
      AcquireCellSettings settings = advancedUI
            .build(); // get the full set of settings from the advanced UI.

      //Now override the advanced settings with what was set in this UI.
      settings.pwsEnabled = this.pwsCBPanel.isSelected();
      settings.pwsSettings.exposure = this.pwsSettings.getExposure();

      settings.dynEnabled = this.dynCBPanel.isSelected();
      settings.dynSettings.exposure = this.dynSettings.getExposure();

      settings.fluorEnabled = this.fluorCBPanel.isSelected();
      settings.fluorSettings = fluorSettings.build();

      return settings;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      //We subscribe to the Globals property changes. This gets fired when a change is detected.
      if (evt.getPropertyName().equals("config")) {
         HWConfiguration cfg = (HWConfiguration) evt.getNewValue();
         setConfigNames(cfg.getSettings().configs);
      }
   }

   private void setConfigNames(List<ImagingConfigurationSettings> settings) {
      List<String> normalNames = new ArrayList<>();
      List<String> spectralNames = new ArrayList<>();
      for (ImagingConfigurationSettings setting : settings) {
         if (setting.configType == ImagingConfiguration.Types.StandardCamera) {
            normalNames.add(setting.name);
         } else if (setting.configType == ImagingConfiguration.Types.SpectralCamera) {
            spectralNames.add(setting.name);
         }
      }
      List<String> allNames = new ArrayList<>();
      allNames.addAll(normalNames);
      allNames.addAll(spectralNames);
      for (BuilderJPanel flSettings : fluorSettings.getSubComponents()) {
         ((FluorPanel) flSettings).setAvailableConfigNames(allNames);
      }
   }
}


class SimplePWSPanel extends JPanel {

   private final ImprovedComponents.FormattedTextField exposure =
         new ImprovedComponents.FormattedTextField(
               NumberFormat.getNumberInstance());

   public SimplePWSPanel() {
      super(new MigLayout());

      exposure.setColumns(6);
      exposure.setValue(
            50.0); //Set a value just so it's not blank. In practice this should be overwritten by saved settings.

      this.add(new JLabel("Exposure (ms):"), "gapleft push");
      this.add(exposure, "wrap");
   }

   public double getExposure() {
      return ((Number) exposure.getValue()).doubleValue();
   }

   public void setExposure(double exp) {
      exposure.setValue(exp);
   }
}

class AdvancedAcquireCellUI extends BuilderJPanel<AcquireCellSettings> implements
      PropertyChangeListener {

   //Allows full configuration of the acquisition settings.
   private final PWSPanel pwsSettings = new PWSPanel();
   private final DynPanel dynSettings = new DynPanel();
   private final ListCardUI<List<FluorSettings>, FluorSettings> fluorSettings = new ListCardUI(
         ArrayList.class, "", new FluorSettings());
   private final CheckBoxPanel pwsCBPanel = new CheckBoxPanel(pwsSettings, "PWS");
   private final CheckBoxPanel dynCBPanel = new CheckBoxPanel(dynSettings, "Dynamics");
   private final CheckBoxPanel fluorCBPanel = new CheckBoxPanel(fluorSettings, "Fluorescence");

   public AdvancedAcquireCellUI() {
      super(new MigLayout("insets 0 0 0 0"), AcquireCellSettings.class);
      Globals.addPropertyChangeListener(this);

      pwsSettings.setBorder(BorderFactory.createEtchedBorder());
      dynSettings.setBorder(BorderFactory.createEtchedBorder());
      fluorSettings.setBorder(BorderFactory.createEtchedBorder());

      this.add(pwsCBPanel, "wrap, span");
      this.add(dynCBPanel, "wrap, span");
      this.add(fluorCBPanel, "wrap, span");

      this.setConfigNames(
            new ArrayList()); //We can't yet reference Globals on initialization. at least initialize an empty state. the property change listener should get fired afterward.
   }

   @Override
   public AcquireCellSettings build() throws BuilderJPanel.BuilderPanelException {
      AcquireCellSettings settings = new AcquireCellSettings();
      settings.pwsEnabled = pwsCBPanel.isSelected();
      settings.pwsSettings = pwsSettings.build();

      settings.dynEnabled = dynCBPanel.isSelected();
      settings.dynSettings = dynSettings.build();

      settings.fluorEnabled = fluorCBPanel.isSelected();
      settings.fluorSettings = fluorSettings.build();
      return settings;
   }

   @Override
   public void populateFields(AcquireCellSettings settings)
         throws BuilderJPanel.BuilderPanelException {
      this.pwsCBPanel.setSelected(settings.pwsEnabled);
      this.pwsSettings.populateFields(settings.pwsSettings);

      this.dynCBPanel.setSelected(settings.dynEnabled);
      this.dynSettings.populateFields(settings.dynSettings);

      this.fluorCBPanel.setSelected(settings.fluorEnabled);
      this.fluorSettings.populateFields(settings.fluorSettings);
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      //We subscribe to the Globals property changes. This gets fired when a change is detected.
      if (evt.getPropertyName().equals("config")) {
         HWConfiguration cfg = (HWConfiguration) evt.getNewValue();
         setConfigNames(cfg.getSettings().configs);
      }
   }

   private void setConfigNames(List<ImagingConfigurationSettings> settings) {
      List<String> normalNames = new ArrayList<>();
      List<String> spectralNames = new ArrayList<>();
      for (ImagingConfigurationSettings setting : settings) {
         if (setting.configType == ImagingConfiguration.Types.StandardCamera) {
            normalNames.add(setting.name);
         } else if (setting.configType == ImagingConfiguration.Types.SpectralCamera) {
            spectralNames.add(setting.name);
         }
      }
      this.pwsSettings.setAvailableConfigNames(spectralNames);
      this.dynSettings.setAvailableConfigNames(spectralNames);
      List<String> allNames = new ArrayList<>();
      allNames.addAll(normalNames);
      allNames.addAll(spectralNames);
      for (BuilderJPanel flSettings : fluorSettings.getSubComponents()) {
         ((FluorPanel) flSettings).setAvailableConfigNames(allNames);
      }
   }
}