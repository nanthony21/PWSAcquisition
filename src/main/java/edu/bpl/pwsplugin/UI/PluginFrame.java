///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2019
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

package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.PWSPlugin;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.SequencerUI;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.RootStep;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.internal.utils.WindowPositioning;

/**
 * @author Nick Anthony
 */
public class PluginFrame extends JFrame {

   private final JTabbedPane tabs = new JTabbedPane();
   private final AcquisitionPanel acqPanel = new AcquisitionPanel();
   private final SequencerUI sequencePanel = Globals.sequencer().ui();
   private final UtilityPanel utilityPanel = new UtilityPanel();
   private final ConfigurationDialog configDialog = new ConfigurationDialog(this);

   public PluginFrame() {
      super("PWS Plugin");
      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            MMStudio.class.getResource(
                  "/org/micromanager/icons/microscope.gif")));

      WindowPositioning.setUpBoundsMemory(this, PluginFrame.class, null);
      this.setLayout(new MigLayout("insets 2 2 2 2"));
      this.setTitle(String.format("%s %s", PWSPlugin.menuName, PWSPlugin.versionNumber));
      this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      this.setResizable(false);

      JMenuBar ma = new JMenuBar();
      JMenu mb = new JMenu("Advanced");
      JMenuItem mc = new JMenuItem("Configuration");
      ma.add(mb);
      mb.add(mc);
      mc.addActionListener((evt) -> {
         HWConfigurationSettings newSettings = this.configDialog.showDialog();
         Globals.setHardwareConfigurationSettings(newSettings);
      });

      this.setJMenuBar(ma);

      this.add(tabs, "wrap, span, grow");
      tabs.addTab("Sequencing", this.sequencePanel);
      tabs.addTab("Quick Acquire", this.acqPanel);
      tabs.add("Utility", this.utilityPanel);

      this.pack();
      this.setMinimumSize(this.getSize());
   }

   public PWSPluginSettings getSettings() {
      //Produce an instance of PWSPluginSettings describing the current status of the UI.
      PWSPluginSettings set = new PWSPluginSettings();
      try {
         set.acquisitionSettings = this.acqPanel.getAcqSettings();
      } catch (BuilderJPanel.BuilderPanelException | NullPointerException e) {
         ReportingUtils.logError(e);
         ReportingUtils
               .showError("Failed to get acquisition settings from UI. See CoreLog for details.");
      }
      set.hwConfiguration = this.configDialog.build();
      set.saveDir = this.acqPanel.getDirectory();
      set.cellNum = this.acqPanel.getCellNumber();
      try {
         set.sequenceRoot = this.sequencePanel.build();
      } catch (BuilderJPanel.BuilderPanelException e) {
         set.sequenceRoot = (RootStep) new RootStepFactory().createStep();
      }
      return set;
   }

   @Override
   public void dispose() {
      Globals.saveSettings(this.getSettings());
      super.dispose();
   }

   public final void populateFields(PWSPluginSettings set) {
      //Update the UI to match the provided settings.
      try {
         this.acqPanel.setAcqSettings(set.acquisitionSettings);
      } catch (NullPointerException | BuilderJPanel.BuilderPanelException e) {
         ReportingUtils.logError(e);
      } //Sometimes a bit of settings will be missing if the code is changed. Don't let that crash the program.
      try {
         this.configDialog.populateFields(set.hwConfiguration);
      } catch (NullPointerException e) {
         ReportingUtils.logError(e);
      }
      try {
         this.acqPanel.setDirectory(set.saveDir);
      } catch (NullPointerException e) {
         ReportingUtils.logError(e);
      }
      try {
         this.acqPanel.setCellNumber(set.cellNum);
      } catch (NullPointerException e) {
         ReportingUtils.logError(e);
      }
      try {
         this.sequencePanel.populateFields(set.sequenceRoot);
      } catch (NullPointerException e) {
         ReportingUtils.logError(e);
      }
   }

   public void setActionButtonsEnabled(boolean enable) {
      utilityPanel.setActionButtonsEnabled(enable);
      acqPanel.setActionButtonsEnabled(enable);
      sequencePanel.setActionButtonsEnabled(enable);
   }
}


