/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class SoftwareAutofocusFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return SoftwareAutoFocusUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return SoftwareAutoFocusSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return SoftwareAutofocus.class;
    }
    
    @Override
    public String getDescription() {
        return "Run a software autofocus routine.";
    }
    
    @Override
    public String getName() {
        return "Software Autofocus";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.UTIL;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.AF;
    }
}

class SoftwareAutoFocusUI extends BuilderJPanel<SoftwareAutoFocusSettings> {
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

class SoftwareAutoFocusSettings extends SequencerSettings {
    public String afPluginName = "OughtaFocus";
}

class SoftwareAutofocus extends EndpointStep {
         
    public SoftwareAutofocus() {
        super(Consts.Type.AF);
    }
    
    @Override
    public SequencerFunction getFunction() {
        SoftwareAutoFocusSettings settings = (SoftwareAutoFocusSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                Globals.mm().getAutofocusManager().setAutofocusMethodByName(settings.afPluginName);
                Globals.mm().getAutofocusManager().getAutofocusMethod().fullFocus();
                return status;
            } 
        };
    }
}