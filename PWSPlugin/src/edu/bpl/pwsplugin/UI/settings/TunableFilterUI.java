/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author N2-LiveCell
 */
public class TunableFilterUI extends SingleBuilderJPanel<TunableFilterSettings>{
    private final JComboBox<String> name = new JComboBox<>();
    
    public TunableFilterUI() {
        super(new MigLayout(), TunableFilterSettings.class);
        
        
        this.add(new JLabel("Device Name:"), "gapleft push");
        this.add(name, "wrap");
        
        this.name.setModel(new DefaultComboBoxModel<>(new Vector<String>(Globals.getMMConfigAdapter().getAllDevices())));
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        return map;
    }
}
