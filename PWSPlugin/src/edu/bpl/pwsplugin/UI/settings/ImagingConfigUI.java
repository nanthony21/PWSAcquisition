/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author N2-LiveCell
 */
public class ImagingConfigUI extends SingleBuilderJPanel<PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings>{
    private CamUI camSettings = new CamUI();
    private TunableFilterUI filtSettings = new TunableFilterUI();
    private JComboBox<ImagingConfiguration.Types> typeCombo = new JComboBox<>();
    private JTextField name = new JTextField(10);
    
    public ImagingConfigUI() {
        super(new MigLayout(), PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings.class);
        
        typeCombo.setModel(new DefaultComboBoxModel<>(ImagingConfiguration.Types.values()));
        
        this.camSettings.setBorder(BorderFactory.createLoweredBevelBorder());
        this.filtSettings.setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.add(new JLabel("Name:"), "gapleft push");
        this.add(this.name, "wrap");
        this.add(new JLabel("Type:"), "gapleft push");
        this.add(this.typeCombo, "wrap");
        this.add(new JLabel("Camera:"), "wrap");
        this.add(this.camSettings, "wrap, span");
        this.add(new JLabel("Tunable Filter:"), "wrap");
        this.add(this.filtSettings, "span");
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("configType", typeCombo);
        map.put("camSettings", camSettings);
        map.put("filtSettings", filtSettings);
        return map;
    }
}
