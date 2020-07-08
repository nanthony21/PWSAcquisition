/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class IlluminatorUI extends BuilderJPanel<IlluminatorSettings> {
    private final JComboBox<String> deviceName = new JComboBox<>();
    
    public IlluminatorUI() {
        super(new MigLayout(), IlluminatorSettings.class);
        
        
        super.add(new JLabel("Device Name:"), "gapleft push");
        super.add(deviceName, "wrap");
        
        this.updateComboBoxes();
    }
    
    private void updateComboBoxes() {
        this.deviceName.setModel(new DefaultComboBoxModel<>(new Vector<String>(Globals.getMMConfigAdapter().getConnectedShutters())));
    }
    
    @Override
    public void populateFields(IlluminatorSettings settings) {
        deviceName.setSelectedItem(settings.name);
    }
    
    @Override
    public IlluminatorSettings build() throws BuilderPanelException {
        IlluminatorSettings settings = new IlluminatorSettings();
        settings.name = (String) deviceName.getSelectedItem();
        return settings;
    }
}
