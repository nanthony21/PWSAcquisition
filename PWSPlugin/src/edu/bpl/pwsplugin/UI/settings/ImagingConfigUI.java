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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
    
    public ImagingConfigUI() {
        super(new MigLayout(), PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings.class);
        
        typeCombo.setModel(new DefaultComboBoxModel<>(ImagingConfiguration.Types.values()));
        
        this.add(new JLabel("Type:"), "gapleft push");
        this.add(this.typeCombo, "wrap");
        this.add(this.camSettings, "wrap");
        this.add(this.filtSettings);
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("configType", typeCombo);
        map.put("camSettings", camSettings);
        map.put("filtSettings", filtSettings);
        return map;
    }
}
