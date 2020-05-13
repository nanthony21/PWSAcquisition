/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class SoftwareAutoFocusUI extends BuilderJPanel<SoftwareAutoFocusSettings> {
    JComboBox<String> afNames = new JComboBox<>();
    
    public SoftwareAutoFocusUI() {
        super(new MigLayout(), SoftwareAutoFocusSettings.class);
        afNames.setModel(new DefaultComboBoxModel<>(Globals.mm().getAutofocusManager().getAllAutofocusMethods().toArray(new String[0])));
        
        this.add(new JLabel("Autofocus Method:"));
        this.add(afNames);
    }
    
    @Override
    public SoftwareAutoFocusSettings build() {
        SoftwareAutoFocusSettings afs = new SoftwareAutoFocusSettings();
        afs.afPluginName = (String) afNames.getSelectedItem();
        return afs;
    }
    
    @Override
    public void populateFields(SoftwareAutoFocusSettings settings) {
        afNames.setSelectedItem(settings.afPluginName);
    }
}
