/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.disablePanel.DisabledPanel;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author N2-LiveCell
 */
public class ImagingConfigUI extends SingleBuilderJPanel<ImagingConfigurationSettings>{
    private CamUI camSettings = new CamUI();
    private TunableFilterUI filtSettings = new TunableFilterUI();
    private JComboBox<ImagingConfiguration.Types> typeCombo = new JComboBox<>();
    private JTextField name = new JTextField(10);
    private DisabledPanel filtSettingsPanel;
    private JComboBox<String> configGroup = new JComboBox<>();
    private JComboBox<String> configState = new JComboBox<>();
    
    public ImagingConfigUI() {
        super(new MigLayout(), ImagingConfigurationSettings.class);
        
        JPanel p = new JPanel(new MigLayout("insets 0 0 0 0, fill"));
        p.add(new JLabel("Tunable Filter:"), "wrap");
        p.add(filtSettings);
        filtSettingsPanel = new DisabledPanel(p);
        
        typeCombo.setModel(new DefaultComboBoxModel<>(ImagingConfiguration.Types.values()));
        
        this.camSettings.setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.filtSettings.setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.typeCombo.addActionListener((evt)->{
            if (this.typeCombo.getSelectedItem() == ImagingConfiguration.Types.StandardCamera) {
                this.filtSettingsPanel.setEnabled(false); // filter settings do not apply to a standard camera.
            } else {
                this.filtSettingsPanel.setEnabled(true);
            }
        });
        
        this.configGroup.addItemListener((evt)->{
            String[] confs = Globals.core().getAvailableConfigs((String) this.configGroup.getSelectedItem()).toArray();
            this.configState.setModel(new DefaultComboBoxModel<>(confs));
        });
        
        this.configGroup.setModel(new DefaultComboBoxModel<>(Globals.core().getAvailableConfigGroups().toArray()));
        
        this.add(new JLabel("Name:"), "gapleft push");
        this.add(this.name, "wrap");
        this.add(new JLabel("Type:"), "gapleft push");
        this.add(this.typeCombo, "wrap");
        this.add(new JLabel("Config Group:"), "gapleft push");
        this.add(configGroup, "wrap");
        this.add(new JLabel("Config Name:"), "gapleft push");
        this.add(configState, "wrap");
        this.add(new JLabel("Camera:"), "wrap");
        this.add(this.camSettings, "wrap, span");
        this.add(this.filtSettingsPanel, "span, wrap");
        //this.add(this.filtSettings, "span, wrap");
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("configType", typeCombo);
        map.put("camSettings", camSettings);
        map.put("filtSettings", filtSettings);
        map.put("configurationGroup", configGroup);
        map.put("configurationName", configState);
        return map;
    }
}
