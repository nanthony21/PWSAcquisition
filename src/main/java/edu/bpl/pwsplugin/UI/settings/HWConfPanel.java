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
import edu.bpl.pwsplugin.UI.utils.ListScrollUI;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * @author nick
 */
public class HWConfPanel extends BuilderJPanel<HWConfigurationSettings> {

   private final JTextField sysNameEdit = new JTextField(20);
   private final JLabel configsFoundLabel = new JLabel("Imaging Configurations: 0");
   private final JButton editConfigsButton = new JButton("Edit Imaging Configurations");
   private List<ImagingConfigurationSettings> configs = new ArrayList<>();


   public HWConfPanel() {
      super(new MigLayout(), HWConfigurationSettings.class);

      this.editConfigsButton.addActionListener((evt) -> {
         ImagingConfigDlg dlg = new ImagingConfigDlg(SwingUtilities.getWindowAncestor(this),
               this.configs);
         List<ImagingConfigurationSettings> results = dlg.showDialog();
         if (results != null) { // new settings were accepted in the dialog
            HWConfigurationSettings config = this.build();
            config.configs = results;
            this.populateFields(config);
         }
      });

      this.add(new JLabel("System Name:"), "gapleft push");
      this.add(this.sysNameEdit, "wrap");
      this.add(configsFoundLabel);
      this.add(this.editConfigsButton, "span");
   }

   @Override
   public HWConfigurationSettings build() {
      HWConfigurationSettings conf = new HWConfigurationSettings();
      conf.systemName = this.sysNameEdit.getText();
      conf.configs = this.configs;
      return conf;
   }

   @Override
   public void populateFields(HWConfigurationSettings config) {
      this.sysNameEdit.setText(config.systemName);
      this.configs = config.configs;
      this.configsFoundLabel.setText("Imaging Configurations: " + config.configs.size());
   }

}

class ImagingConfigDlg extends JDialog {

   private ListScrollUI<List<ImagingConfigurationSettings>, ImagingConfigurationSettings> configs;
   private JButton acceptButton = new JButton("Accept");
   private JButton cancelButton = new JButton("Cancel");
   List<ImagingConfigurationSettings> result = null;

   public ImagingConfigDlg(Window owner, List<ImagingConfigurationSettings> currentConfigs) {
      super(owner, "Imaging Configurations");
      this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      this.setLocationRelativeTo(owner);
      this.setModal(true);
      this.setContentPane(new JPanel(new MigLayout()));

      ImagingConfigurationSettings defaultConfig = new ImagingConfigurationSettings();

      this.configs = new ListScrollUI<>(
            (Class<List<ImagingConfigurationSettings>>) (Object) ArrayList.class, defaultConfig);

      try {
         this.configs.populateFields(currentConfigs);
      } catch (BuilderJPanel.BuilderPanelException e) {
         ReportingUtils.showError(e);
         ReportingUtils.logError(e);
      }

      this.acceptButton.addActionListener((evt) -> { //Set the result and then close the dialog.
         try {
            this.result = configs.build();
         } catch (BuilderJPanel.BuilderPanelException e) {
            Globals.mm().logs().showError(e);
            return;
         }
         this.setVisible(false);
         this.dispose();
      });

      this.cancelButton
            .addActionListener((evt) -> { // Close the dialog while leaving `result` as `null`
               this.setVisible(false);
               this.dispose();
            });

      this.add(configs, "wrap, span");
      this.add(acceptButton);
      this.add(cancelButton);
      this.pack();
   }

   public List<ImagingConfigurationSettings> showDialog() {
      //Open the dialog, block until it is closed, then return the `result`.
      this.setVisible(true);
      return this.result;
   }
}