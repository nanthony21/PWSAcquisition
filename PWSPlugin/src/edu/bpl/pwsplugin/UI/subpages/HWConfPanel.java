/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.subpages;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.ListScrollUI;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.CamSettings;
import edu.bpl.pwsplugin.settings.HWConfiguration;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
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
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
public class HWConfPanel extends BuilderJPanel<HWConfiguration>{
    private JTextField sysNameEdit = new JTextField(20);
    private JButton editConfigsButton = new JButton("Edit Configurations");
    private List<ImagingConfigurationSettings> configs = new ArrayList<>();
    
    public HWConfPanel() {
        super(new MigLayout(), HWConfiguration.class);
        
        ImagingConfigurationSettings defaultConfig = new ImagingConfigurationSettings();
        defaultConfig.configType = ImagingConfiguration.Types.StandardCamera;
        defaultConfig.camSettings = new CamSettings();
        defaultConfig.filtSettings = new TunableFilterSettings();
        
        this.editConfigsButton.addActionListener((evt)->{
            ImagingConfigDlg dlg = new ImagingConfigDlg(SwingUtilities.getWindowAncestor(this), this.configs);
            List<ImagingConfigurationSettings> results = dlg.showDialog();
            if (results != null) {
                this.configs = results;
            }
        });
        
        
        this.add(new JLabel("System Name:"), "gapleft push");
        this.add(this.sysNameEdit, "wrap");
        this.add(this.editConfigsButton, "span");
    }
    
    @Override
    public HWConfiguration build() {
        HWConfiguration conf = new HWConfiguration();
        conf.systemName = this.sysNameEdit.getText();
        conf.configs = this.configs;
        return conf;
    }
    
    @Override
    public void populateFields(HWConfiguration config) {
        this.sysNameEdit.setText(config.systemName);
        this.configs = config.configs;
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
        } catch (Exception e) {
            ReportingUtils.showError(e);
            ReportingUtils.logError(e);
        }
        
        this.acceptButton.addActionListener((evt)->{
            this.result = configs.build();
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