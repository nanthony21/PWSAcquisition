/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings;

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.ChangeConfigGroupSettings;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.events.ConfigGroupChangedEvent;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ChangeConfigGroupUI extends BuilderJPanel<ChangeConfigGroupSettings> implements ItemListener {
    JComboBox<String> configGroupName = new JComboBox<>();
    JComboBox<String> configValue = new JComboBox<>();
    
    public ChangeConfigGroupUI() {
        super(new MigLayout(), ChangeConfigGroupSettings.class);
        
        configGroupName.addItemListener(this);
        
        updateConfigGroupComboBox();
        
        this.add(new JLabel("Group Name:"));
        this.add(configGroupName, "wrap");
        this.add(new JLabel("Setting:"));
        this.add(configValue);
        
        Globals.mm().events().registerForEvents(this);
    }
    
    private void updateConfigGroupComboBox() {
        String[] s = Globals.core().getAvailableConfigGroups().toArray();
        configGroupName.setModel(new DefaultComboBoxModel<>(s));    
    }
    
    @Override
    public void populateFields(ChangeConfigGroupSettings settings) {
        this.configGroupName.setSelectedItem(settings.configGroupName);
        this.configValue.setSelectedItem(settings.configValue);
    }
    
    @Override
    public ChangeConfigGroupSettings build() {
        ChangeConfigGroupSettings settings = new ChangeConfigGroupSettings();
        settings.configGroupName = (String) this.configGroupName.getSelectedItem();
        settings.configValue = (String) this.configValue.getSelectedItem();
        return settings;
    }
    
    @Override
    public void itemStateChanged(ItemEvent evt) { //Fired with the value of the config group changes, update the available values to match the config group
        String[] s = Globals.core().getAvailableConfigs((String) evt.getItem()).toArray();
        configValue.setModel(new DefaultComboBoxModel<>(s));
    }
    
    @Subscribe
    public void onConfigGroupChanged(ConfigGroupChangedEvent evt) {
        updateConfigGroupComboBox();
    }
    
    
}
