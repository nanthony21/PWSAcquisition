/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.Globals;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import mmcorej.StrVector;
import net.miginfocom.swing.MigLayout;
import org.micromanager.PropertyMap;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
public class FluorPanel extends ChangeListenJPanel implements SaveableLoadableUI{
    private JSpinner wvSpinner;
    private JSpinner exposureSpinner;
    private JComboBox filterCombo;
    private JComboBox altCamNameCombo;
    private JCheckBox useAltCamCheckbox = new JCheckBox("Use Alternate Camera");
    
    public FluorPanel() {
        super(new MigLayout());
        
        wvSpinner = new JSpinner(new SpinnerNumberModel(550, 400, 1000, 5));
        exposureSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 5000, 100));
        filterCombo.setModel(this.getFilterComboModel());
        altCamNameCombo.setModel(this.getCameraComboModel());
        
        useAltCamCheckbox.addActionListener((evt)->{
            boolean s = useAltCamCheckbox.isSelected();
            wvSpinner.setEnabled(!s);
            altCamNameCombo.setEnabled(s);
        });
        
        this.addDocumentChangeListeners(new JComponent[] {wvSpinner, exposureSpinner, filterCombo, altCamNameCombo, useAltCamCheckbox});
        
        super.add(new JLabel("Wavelength (nm)"));
        super.add(new JLabel("Exposure (ms)"));
        super.add(new JLabel("Filter Set"), "wrap");
        super.add(wvSpinner);
        super.add(exposureSpinner);
        super.add(filterCombo, "wrap");
        super.add(useAltCamCheckbox);
        super.add(new JLabel("Camera Name"));
        super.add(altCamNameCombo);
    }
    
    @Override
    public PropertyMap toSettings() {
        
    }
    
    @Override
    public void fromSettings(PropertyMap map) {
        
    }
    
    private DefaultComboBoxModel<String> getFilterComboModel() {    
        Iterator<String> filterSettings = Globals.core().getAvailableConfigs("Filter").iterator();
        StrVector settings = new StrVector();
        while (filterSettings.hasNext()) {
            settings.add(filterSettings.next());
        }
        if (settings.size() == 0) {
            Globals.acqManager().automaticFlFilterEnabled = false;
            ReportingUtils.showMessage("Micromanager is missing a `Filter` config group which is needed for automated fluorescence. The first setting of the group should be the filter block used for PWS");
            return new DefaultComboBoxModel();
        } else {
            Globals.acqManager().automaticFlFilterEnabled = true;
            DefaultComboBoxModel model = new DefaultComboBoxModel(settings.toArray());
            return model;
        }
    }
    
    private DefaultComboBoxModel<String> getCameraComboModel() {
        Iterator<String> cameras = Globals.core().getAvailableConfigs("Camera").iterator();
        StrVector camSettings = new StrVector();
        while (cameras.hasNext()) {
            camSettings.add(cameras.next());
        }
        if (camSettings.size()==0) {
            useAltCamCheckbox.setSelected(false);
            useAltCamCheckbox.setEnabled(false);
            ReportingUtils.showMessage("Could not find a `Camera` config group. This group should be set to allow switching between multiple cameras for different imaging modalities.");
            return new DefaultComboBoxModel();
        } else {
            DefaultComboBoxModel model = new DefaultComboBoxModel(camSettings.toArray());
            return model;
        }
    }
        
}