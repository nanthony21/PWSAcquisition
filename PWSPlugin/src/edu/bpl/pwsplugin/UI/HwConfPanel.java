/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.settings.Settings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class HWConfPanel extends SingleBuilderJPanel<Settings.HWConfiguration>{
    private JComboBox tunableFilterCombo = new JComboBox(); //Does this belong here? What about when multiple filters are present.
    private JTextField sysNameEdit = new JTextField();
    private ListCardUI<List<Settings.CamSettings>, Settings.CamSettings> cameras = new ListCardUI<List<Settings.CamSettings>, Settings.CamSettings>("Camera:");
    
    public HWConfPanel() {
        super(new MigLayout(), Settings.HWConfiguration.class);
        
    }
    
    @Override
    public Map<String, JComponent> getPropertyFieldMap() {
        //TODO fix
        Map<String, JComponent> map = new HashMap<String, JComponent>();
        map.put("wvStart", wvStartSpinner);
        map.put("wvStop", wvStopSpinner);
        map.put("wvStep", wvStepSpinner);
        map.put("exposure", exposureSpinner);
        map.put("ttlTriggering", ttlTriggerCheckbox);
        map.put("externalCamTriggering", externalTriggerCheckBox);
        return map;
    }
}
