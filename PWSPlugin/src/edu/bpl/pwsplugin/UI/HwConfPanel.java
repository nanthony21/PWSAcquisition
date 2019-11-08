/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class HWConfPanel extends SingleBuilderJPanel<HWConfiguration>{
    private JComboBox tunableFilterCombo = new JComboBox();
    private JTextField sysNameEdit = new JTextField();
    private ListCardUI<List<CamSettings>, CamSettings> = new ListCardUI("Camera:");
    
    public HWConfPanel() {
        super(new MigLayout());
        
    }
}
