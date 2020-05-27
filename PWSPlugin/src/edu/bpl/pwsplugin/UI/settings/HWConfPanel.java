/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.ListScrollUI;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.CamSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.TunableFilterSettings;
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
import jdk.nashorn.internal.objects.Global;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
public class HWConfPanel extends BuilderJPanel<HWConfigurationSettings>{
    private JTextField sysNameEdit = new JTextField(20);
    private JLabel configsFoundLabel = new JLabel();
    private JButton editConfigsButton = new JButton("Edit Configurations");
    private List<ImagingConfigurationSettings> configs = new ArrayList<>();
    
    public HWConfPanel() {
        super(new MigLayout(), HWConfigurationSettings.class);
        
        ImagingConfigurationSettings defaultConfig = new ImagingConfigurationSettings();
        defaultConfig.configType = ImagingConfiguration.Types.StandardCamera;
        defaultConfig.camSettings = new CamSettings();
        defaultConfig.filtSettings = new TunableFilterSettings();
        
        this.editConfigsButton.addActionListener((evt)->{
            ImagingConfigDlg dlg = new ImagingConfigDlg(SwingUtilities.getWindowAncestor(this), this.configs);
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
        defaultConfig.configType = ImagingConfiguration.Types.StandardCamera;
        defaultConfig.camSettings = new CamSettings();
        defaultConfig.filtSettings = new TunableFilterSettings();
        this.configs = new ListScrollUI<>((Class<List<ImagingConfigurationSettings>>)(Object) ArrayList.class, defaultConfig);
        
        try {
            this.configs.populateFields(currentConfigs);
        } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.showError(e);
            ReportingUtils.logError(e);
        }
        
        this.acceptButton.addActionListener((evt)->{
            try {
                this.result = configs.build();
            } catch (BuilderJPanel.BuilderPanelException e) {
                Globals.mm().logs().showError(e);
                return;
            }
            this.setVisible(false);
            this.dispose();
        });
        
        this.cancelButton.addActionListener((evt)->{
            this.setVisible(false);
            this.dispose();
        });
        
        this.add(configs, "wrap, span");
        this.add(acceptButton);
        this.add(cancelButton);
        this.pack();
    }
    
    public List<ImagingConfigurationSettings> showDialog() {
        this.setVisible(true);
        return this.result;
    }
}