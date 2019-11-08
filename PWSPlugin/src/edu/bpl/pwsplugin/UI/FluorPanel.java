/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.Globals;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
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
    
    public FluorPanel() {
        super(new MigLayout());
        
        wvSpinner = new JSpinner(new SpinnerNumberModel(550, 400, 1000, 5));
        exposureSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 5000, 100));
        filterCombo.setModel(this.getFilterComboModel());
        
        
        
        
        super.add(new JLabel("Wavelength (nm)"));
        super.add(new JLabel("Exposure (ms)"));
        super.add(new JLabel("Filter Set"), "wrap");
        super.add(wvSpinner);
        super.add(exposureSpinner);
        super.add(filterCombo);

        flFilterBlockCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flFilterBlockComboActionPerformed(evt);
            }
        });


        altCamCheckBox.setText("Use Alternate Camera");
        altCamCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                altCamCheckBoxActionPerformed(evt);
            }
        });

        jLabel4.setText("Camera Name");

        jLabel5.setText("Affine Transform");

        altCamTransformEdit.setText("jTextField2");

        altCamNameCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        altCamNameCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                altCamNameComboActionPerformed(evt);
            }
        });
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
            acqManager_.automaticFlFilterEnabled = false;
            ReportingUtils.showMessage("Micromanager is missing a `Filter` config group which is needed for automated fluorescence. The first setting of the group should be the filter block used for PWS");
            return new DefaultComboBoxModel();
        } else {
            acqManager_.automaticFlFilterEnabled = true;
            DefaultComboBoxModel model = new DefaultComboBoxModel(settings.toArray());
            return model;
        }
    }
        
        Iterator<String> cameras = Globals.core().getAvailableConfigs("Camera").iterator();
        StrVector camSettings = new StrVector();
        while (cameras.hasNext()) {
            camSettings.add(cameras.next());
        }
        if (camSettings.size()==0) {
            altCamCheckBox.setSelected(false);
            altCamCheckBox.setEnabled(false);
            ReportingUtils.showMessage("Could not find a `Camera` config group. This group should be set to allow switching between multiple cameras for different imaging modalities.");
        } else {
            DefaultComboBoxModel model = new DefaultComboBoxModel(camSettings.toArray());
            altCamNameCombo.setModel(model);
        }
    }
        
}
