/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.subpages;

import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class HWConfPanel extends SingleBuilderJPanel<PWSPluginSettings.HWConfiguration>{
    private JTextField sysNameEdit = new JTextField(20);
    
    private ListCardUI<List<PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings>, PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings> configs;
    
    public HWConfPanel() {
        super(new MigLayout(), PWSPluginSettings.HWConfiguration.class);
        
        PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings defaultConfig = new PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings();
        defaultConfig.configType = ImagingConfiguration.Types.StandardCamera;
        defaultConfig.camSettings = new PWSPluginSettings.HWConfiguration.CamSettings();
        defaultConfig.filtSettings = new PWSPluginSettings.HWConfiguration.TunableFilterSettings();
        this.configs = new ListCardUI<>((Class<List<PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings>>)(Object) ArrayList.class, "Imaging Config:", defaultConfig);
        
        this.add(new JLabel("System Name:"), "gapleft push");
        this.add(this.sysNameEdit, "wrap");
        this.add(this.configs, "span");
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("systemName", sysNameEdit);
        map.put("configs", configs);
        return map;
    }
}
