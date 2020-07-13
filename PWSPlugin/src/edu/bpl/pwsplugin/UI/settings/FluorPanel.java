/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import java.util.NoSuchElementException;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class FluorPanel extends BuilderJPanel<FluorSettings>{
    private ImprovedComponents.Spinner wvSpinner;
    private ImprovedComponents.Spinner exposureSpinner;
    private ImprovedComponents.Spinner focusOffsetSpinner;
    private JComboBox<String> filterCombo = new JComboBox<>();
    private JComboBox<String> imConfName = new JComboBox<>();

    
    public FluorPanel() {
        super(new MigLayout(), FluorSettings.class);
        
        wvSpinner = new ImprovedComponents.Spinner(new SpinnerNumberModel(550, 400, 1000, 5));
        exposureSpinner = new ImprovedComponents.Spinner(new SpinnerNumberModel(1000.0, 1.0, 5000.0, 100.0));
        focusOffsetSpinner = new ImprovedComponents.Spinner(new SpinnerNumberModel(0.0, -50.0, 50.0, 1.0));
        
        try {
            List<String> confNames = new ArrayList<>();
            for (ImagingConfigurationSettings setting : Globals.getHardwareConfiguration().getSettings().configs) {
                confNames.add(setting.name);
            }
            imConfName.setModel(new DefaultComboBoxModel<String>(confNames.toArray(new String[confNames.size()])));
        } catch (NullPointerException e) {} //This will often fail during plugin initialization. that's ok, the PropertyChangeListener should also set this once initialization is completed.
        
        imConfName.addItemListener((evt) -> {
            String name = (String) imConfName.getSelectedItem();
            if (Globals.getHardwareConfiguration() != null) { //It can be null on startup
                try {
                    ImagingConfiguration conf = Globals.getHardwareConfiguration().getImagingConfigurationByName(name);
                    wvSpinner.setEnabled(conf.hasTunableFilter());
                    configureFilterCombo(conf.getFluorescenceConfigGroup());
                } catch (NoSuchElementException e) {}
            }
        });
                
        super.add(new JLabel("Wavelength (nm)"));
        super.add(new JLabel("Exposure (ms)"));
        super.add(new JLabel("Filter Set"), "wrap");
        super.add(wvSpinner);
        super.add(exposureSpinner);
        super.add(filterCombo, "wrap");
        super.add(new JLabel("Z Offset (um)"));
        super.add(new JLabel("Imaging Configuration"), "span, wrap");
        super.add(focusOffsetSpinner);
        super.add(imConfName, "span");
    }
    
    
    private void configureFilterCombo(String configGroup) {  
        if (configGroup != null) {
            filterCombo.setEnabled(true);
            String[] filters;
            try { // Allow the panel to show up even if we don't have our connection to micromanager working (useful for testing).
                filters = Globals.core().getAvailableConfigs(configGroup).toArray();
            } catch (NullPointerException e) {
                filters = new String[] {"None!"};
            }
            filterCombo.setModel(new DefaultComboBoxModel<>(filters));
        } else { //Manual filter switching, disable control
            filterCombo.setModel(new DefaultComboBoxModel<>());
            filterCombo.setEnabled(false);
        }
    }  
    
    @Override
    public FluorSettings build() {
        FluorSettings settings = new FluorSettings();
        settings.exposure = (Double) this.exposureSpinner.getValue();
        settings.filterConfigName = (String) this.filterCombo.getSelectedItem();
        settings.focusOffset = (Double) this.focusOffsetSpinner.getValue();
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
        return this.filterCombo.getSelectedItem() == filter; //Selection won't change if the above command didn't work
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
        if (names.isEmpty()) {
            this.imConfName.addItem("NONE!"); //Prevent a null pointer error.
        } else {
            for (String name : names) {
                this.imConfName.addItem(name);
            }
        }
    }
}
