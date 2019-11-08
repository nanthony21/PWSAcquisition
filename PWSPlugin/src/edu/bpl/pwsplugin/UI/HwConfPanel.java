/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class HWConfPanel extends ChangeListenJPanel{
    private JComboBox tunableFilterCombo = new JComboBox();
    private JTextField sysNameEdit = new JTextField();
    
    
    public HWConfPanel() {
        super(new MigLayout());
        
    }
}
