/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.subpages;

import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class HWConfPanel extends SingleBuilderJPanel<PWSPluginSettings.HWConfiguration>{
    private JTextField sysNameEdit = new JTextField();
    private ListCardUI<PWSPluginSettings.HWConfiguration.CamSettings> cameras = new ListCardUI<PWSPluginSettings.HWConfiguration.CamSettings>((Class<List<PWSPluginSettings.HWConfiguration.CamSettings>>)(Object) ArrayList.class, "Camera:");
    
    public HWConfPanel() {
        super(new MigLayout(), PWSPluginSettings.HWConfiguration.class);
        
    }
    
    @Override
    public Map<String, JComponent> getPropertyFieldMap() {
        Map<String, JComponent> map = new HashMap<String, JComponent>();
        map.put("systemName", sysNameEdit);
        map.put("cameras", cameras);
        return map;
    }
}