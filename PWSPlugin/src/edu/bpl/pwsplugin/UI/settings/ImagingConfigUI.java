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
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author N2-LiveCell
 */
public class ImagingConfigUI extends SingleBuilderJPanel<ImagingConfigurationSettings>{
    private final CamUI camSettings = new CamUI();
    private final TunableFilterUI filtSettings = new TunableFilterUI();
    private final IlluminatorUI illumSettings = new IlluminatorUI();
    private final JComboBox<ImagingConfiguration.Types> typeCombo = new JComboBox<>();
    private final JTextField name = new JTextField(10);
    private final JComboBox<String> configGroup = new JComboBox<>();
    private final JComboBox<String> configState = new JComboBox<>();
    private final JComboBox<String> fluorescenceConfigGroup = new JComboBox<>();
    
    public ImagingConfigUI() {
        super(new MigLayout(), ImagingConfigurationSettings.class);
        
        JPanel p = new JPanel(new MigLayout("insets 0 0 0 0, fill"));
        p.add(new JLabel("Tunable Filter:"), "wrap");
        p.add(filtSettings);
        DisabledPanel filtSettingsPanel = new DisabledPanel(p);//Used to block the filter settings when they are not applicable.
        
        typeCombo.setModel(new DefaultComboBoxModel<>(ImagingConfiguration.Types.values()));
        
        this.camSettings.setBorder(BorderFactory.createLoweredBevelBorder());
        this.filtSettings.setBorder(BorderFactory.createLoweredBevelBorder());
        this.illumSettings.setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.typeCombo.addActionListener((evt)->{
            if (this.typeCombo.getSelectedItem() == ImagingConfiguration.Types.StandardCamera) {
                filtSettingsPanel.setEnabled(false); // filter settings do not apply to a standard camera.
            } else {
                filtSettingsPanel.setEnabled(true);
            }
        });
        
        this.configGroup.addItemListener((evt)->{
            String[] confs = Globals.core().getAvailableConfigs((String) this.configGroup.getSelectedItem()).toArray();
            this.configState.setModel(new DefaultComboBoxModel<>(confs));
        });
        
        String[] configGroups = Globals.core().getAvailableConfigGroups().toArray();
        
        this.configGroup.setModel(new DefaultComboBoxModel<>(configGroups));
        this.configGroup.getItemListeners()[0].itemStateChanged(null); // trigger the itemlistener to initialize
        
        String[] fluorConfigGroups = (String[]) ArrayUtils.addAll(configGroups, new String[] {ImagingConfigurationSettings.MANUALFLUORESCENCENAME});
        this.fluorescenceConfigGroup.setModel(new DefaultComboBoxModel<>(fluorConfigGroups));
        
        this.add(new JLabel("Name:"), "gapleft push");
        this.add(this.name, "wrap");
        this.add(new JLabel("Type:"), "gapleft push");
        this.add(this.typeCombo, "wrap");
        this.add(new JLabel("Config Group:"), "gapleft push");
        this.add(configGroup, "wrap");
        this.add(new JLabel("Config Name:"), "gapleft push");
        this.add(configState, "wrap");
        this.add(new JLabel("Fluor. Filter Group:"), "gapleft push");
        this.add(fluorescenceConfigGroup, "wrap");
        this.add(new JLabel("Camera:"), "wrap");
        this.add(this.camSettings, "wrap, span");
        this.add(new JLabel("Illuminator:"), "wrap");
        this.add(this.illumSettings, "wrap, span");
        this.add(filtSettingsPanel, "span, wrap");
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("configType", typeCombo);
        map.put("camSettings", camSettings);
        map.put("filtSettings", filtSettings);
        map.put("illuminatorSettings", illumSettings);
        map.put("configurationGroup", configGroup);
        map.put("configurationName", configState);
        return map;
    }
}
