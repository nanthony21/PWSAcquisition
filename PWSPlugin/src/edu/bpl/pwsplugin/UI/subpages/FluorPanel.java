/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.subpages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class FluorPanel extends SingleBuilderJPanel<PWSPluginSettings.FluorSettings>{
    private JSpinner wvSpinner;
    private JSpinner exposureSpinner;
    private JComboBox<String> filterCombo = new JComboBox<>();
    private JComboBox<String> altCamNameCombo = new JComboBox<>();
    private JCheckBox useAltCamCheckbox = new JCheckBox("Use Alternate Camera");
    
    public FluorPanel() {
        super(new MigLayout(), PWSPluginSettings.FluorSettings.class);
        
        wvSpinner = new JSpinner(new SpinnerNumberModel(550, 400, 1000, 5));
        exposureSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 5000, 100));
        filterCombo.setModel(this.getFilterComboModel());
        altCamNameCombo.setModel(this.getCameraComboModel());
        
        useAltCamCheckbox.addActionListener((evt)->{
            boolean s = useAltCamCheckbox.isSelected();
            wvSpinner.setEnabled(!s);
            altCamNameCombo.setEnabled(s);
        });
                
        super.add(new JLabel("Wavelength (nm)"));
        super.add(new JLabel("Exposure (ms)"));
        super.add(new JLabel("Filter Set"), "wrap");
        super.add(wvSpinner);
        super.add(exposureSpinner);
        super.add(filterCombo, "wrap");
        super.add(useAltCamCheckbox, "wrap, span");
        super.add(new JLabel("Camera Name:"));
        super.add(altCamNameCombo);
    }
    
    
    private DefaultComboBoxModel<String> getFilterComboModel() {    
        return new DefaultComboBoxModel<>((String[]) Globals.getMMConfigAdapter().getFilters().toArray());
    }
    
    private DefaultComboBoxModel<String> getCameraComboModel() {
        return new DefaultComboBoxModel<>((String[]) Globals.getMMConfigAdapter().getConnectedCameras().toArray());
    }
    
    @Override
    public Map<String, JComponent> getPropertyFieldMap() {
        Map<String, JComponent> map = new HashMap<String, JComponent>();
        map.put("exposure", exposureSpinner);
        map.put("filterConfigName", filterCombo);
        map.put("useAltCamera", useAltCamCheckbox);
        map.put("altCamName", altCamNameCombo);
        map.put("tfWavelength", wvSpinner);
        return map;
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
}
