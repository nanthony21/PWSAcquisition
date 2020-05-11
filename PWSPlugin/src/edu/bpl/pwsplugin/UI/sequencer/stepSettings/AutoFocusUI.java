/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.stepSettings;

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
public class AutoFocusUI extends BuilderJPanel<SoftwareAutoFocusSettings> {
    JComboBox<String> afNames = new JComboBox<>();
    
    public AutoFocusUI() {
        super(new MigLayout(), SoftwareAutoFocusSettings.class);
        afNames.setModel(new DefaultComboBoxModel<>((String[]) Globals.mm().getAutofocusManager().getAllAutofocusMethods().toArray()));
        
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
