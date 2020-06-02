/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.UI.settings.HWConfPanel;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
class ConfigurationDialog extends JDialog {
    JButton acceptButton = new JButton("Accept");
    private HWConfPanel hwc = new HWConfPanel();
    
    public ConfigurationDialog(Window owner) {
        super(owner, "Hardware Configuration");
        this.setModal(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(owner);
        
        acceptButton.addActionListener((evt)->{
            this.setVisible(false);
            this.dispose();
        });
        
        JPanel pnl = new JPanel(new MigLayout());
        pnl.add(hwc, "wrap");
        pnl.add(acceptButton, "span, align center");
        this.setContentPane(pnl);
        this.pack();
    }
    
    public void populateFields(HWConfigurationSettings config) {
        hwc.populateFields(config);
    }
    
    public HWConfigurationSettings build() {
        return hwc.build();
    }
    
    public HWConfigurationSettings showDialog() {
        this.setVisible(true);
        return this.build();
    }
}
