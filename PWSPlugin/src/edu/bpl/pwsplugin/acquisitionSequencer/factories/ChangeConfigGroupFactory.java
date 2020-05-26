/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.events.ConfigGroupChangedEvent;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ChangeConfigGroupFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return ChangeConfigGroupUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return ChangeConfigGroupSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return ChangeConfigGroup.class;
    }
    
    @Override
    public String getDescription() {
        return "Change one of the Micro-Manager configuration groups. E.G. you could change the objective, etc.";
    }
    
    @Override
    public String getName() {
        return "Change Configuration Group";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.UTIL;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.CONFIG;
    }
}

class ChangeConfigGroupUI extends BuilderJPanel<ChangeConfigGroupSettings> implements ItemListener {
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

class ChangeConfigGroupSettings extends SequencerSettings {
    public String configGroupName;
    public String configValue;
}

class ChangeConfigGroup extends ContainerStep {
    public ChangeConfigGroup() {
        super(Consts.Type.CONFIG);
    }

    @Override
    public SequencerFunction getFunction() {
        SequencerFunction subStepFunc = getSubstepsFunction();
        ChangeConfigGroupSettings settings = (ChangeConfigGroupSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                String origConfValue = Globals.core().getCurrentConfig(settings.configGroupName);
                status.update(String.format("Changing %s config group to %s", settings.configGroupName, settings.configValue), status.currentCellNum);
                Globals.core().setConfig(settings.configGroupName, settings.configValue);
                status = subStepFunc.apply(status);
                Globals.core().setConfig(settings.configGroupName, origConfValue);
                status.update(String.format("Changing %s config group back to original setting, %s", settings.configGroupName, origConfValue), status.currentCellNum);
                return status;
            }
        };
    }  
}