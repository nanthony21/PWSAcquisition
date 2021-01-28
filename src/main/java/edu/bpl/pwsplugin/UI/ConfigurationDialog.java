///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
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
        this.setResizable(false);
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
