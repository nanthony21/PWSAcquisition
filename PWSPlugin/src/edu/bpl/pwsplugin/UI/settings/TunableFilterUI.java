/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author N2-LiveCell
 */
public class TunableFilterUI extends SingleBuilderJPanel<PWSPluginSettings.HWConfiguration.TunableFilterSettings>{
    private JTextField name = new JTextField(20);
    private JComboBox<TunableFilter.Types> filterType = new JComboBox<>();
    
    public TunableFilterUI() {
        super(new MigLayout(), PWSPluginSettings.HWConfiguration.TunableFilterSettings.class);
        
        this.filterType.setModel(new DefaultComboBoxModel<>(TunableFilter.Types.values()));
        
        this.add(new JLabel("Device Name:"), "gapleft push");
        this.add(name, "wrap");
        this.add(new JLabel("Type:"), "gapleft push");
        this.add(filterType, "wrap");
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("filterType", filterType);
        return map;
    }
}
