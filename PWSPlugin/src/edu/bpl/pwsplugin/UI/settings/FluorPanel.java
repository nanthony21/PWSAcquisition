/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class FluorPanel extends BuilderJPanel<FluorSettings>{
    private JSpinner wvSpinner;
    private JSpinner exposureSpinner;
    private JSpinner focusOffsetSpinner;
    private JComboBox<String> filterCombo = new JComboBox<>();
    private JComboBox<String> imConfName = new JComboBox<>();

    
    public FluorPanel() {
        super(new MigLayout(), FluorSettings.class);
        
        wvSpinner = new JSpinner(new SpinnerNumberModel(550, 400, 1000, 5));
        exposureSpinner = new JSpinner(new SpinnerNumberModel(1000.0, 1.0, 5000.0, 100.0));
        focusOffsetSpinner = new JSpinner(new SpinnerNumberModel(0, -10000, 10000, 100));
        filterCombo.setModel(this.getFilterComboModel());
        
        try {
            List<String> confNames = new ArrayList<>();
            for (ImagingConfigurationSettings setting : Globals.getHardwareConfiguration().getSettings().configs) {
                confNames.add(setting.name);
            }
            imConfName.setModel(new DefaultComboBoxModel<String>(confNames.toArray(new String[confNames.size()])));
        } catch (NullPointerException e) {} //This will often fail during plugin initialization. that's ok, the PropertyChangeListener should also set this once initialization is completed.
                
        super.add(new JLabel("Wavelength (nm)"));
        super.add(new JLabel("Exposure (ms)"));
        super.add(new JLabel("Filter Set"), "wrap");
        super.add(wvSpinner);
        super.add(exposureSpinner);
        super.add(filterCombo, "wrap");
        super.add(new JLabel("Focus Offset"));
        super.add(new JLabel("Imaging Configuration"), "span, wrap");
        super.add(focusOffsetSpinner);
        super.add(imConfName, "span");
    }
    
    
    private DefaultComboBoxModel<String> getFilterComboModel() {    
        try { // Allow the panel to show up even if we don't have our connection to micromanager working (useful for testing).
            return new DefaultComboBoxModel<>((String[]) Globals.getMMConfigAdapter().getFilters().toArray());
        } catch (NullPointerException e) {
            String[] filts = {"None!"};
            return new DefaultComboBoxModel<>(filts);
        }
    }  
    
    @Override
    public FluorSettings build() {
        FluorSettings settings = new FluorSettings();
        settings.exposure = (Double) this.exposureSpinner.getValue();
        settings.filterConfigName = (String) this.filterCombo.getSelectedItem();
        settings.focusOffset = (Integer) this.focusOffsetSpinner.getValue();
        settings.tfWavelength = (Integer) this.wvSpinner.getValue();
        settings.imConfigName = (String) this.imConfName.getSelectedItem();
        return settings;
    }
    
    @Override
    public void populateFields(FluorSettings settings) {
        this.exposureSpinner.setValue(settings.exposure);
        this.filterCombo.setSelectedItem(settings.filterConfigName);
        this.wvSpinner.setValue(settings.tfWavelength);
        this.imConfName.setSelectedItem(settings.imConfigName);
        this.focusOffsetSpinner.setValue(settings.focusOffset);
    }
    
    //API
    public boolean setFluorescenceFilter(String filter) { //Returns true if success
        this.filterCombo.setSelectedItem(filter);
        if (this.filterCombo.getSelectedItem() != filter) {//Selection won't change if the above command didn't work
            return false;
        } else {
            return true;
        }
    }
    
    public String getSelectedFilterName() {
        return this.filterCombo.getSelectedItem().toString();
    }
    
    public List<String> getFluorescenceFilterNames() {
        List<String> names = new ArrayList<String>();
        for (int i=0; i<this.filterCombo.getItemCount(); i++) {
            names.add(this.filterCombo.getItemAt(i));
        }
        return names;
    }
    
    public void setExposure(double exposureMs) {
        this.exposureSpinner.setValue(exposureMs);
    }
    
    public void setEmissionWavelength(int wavelength) {
        this.wvSpinner.setValue(wavelength);
    }
    
    public void setAvailableConfigNames(List<String> names) {
        this.imConfName.removeAllItems();
        for (String name : names) {
            this.imConfName.addItem(name);
        }
    }
}
