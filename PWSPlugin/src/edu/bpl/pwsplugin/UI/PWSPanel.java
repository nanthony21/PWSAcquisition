/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class PWSPanel extends ChangeListenJPanel{
    private JSpinner exposureSpinner;
    private JSpinner wvStartSpinner;
    private JSpinner wvStopSpinner;
    private JSpinner wvStepSpinner;
    private JCheckBox ttlTriggerCheckbox = new JCheckBox("Use TTL Sequencing");
    private JCheckBox externalTriggerCheckBox = new JCheckBox("Use External TTL Trigger");
    
    public PWSPanel() {
        super(new MigLayout());
        
        exposureSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 5));
        wvStartSpinner = new JSpinner(new SpinnerNumberModel(500, 400, 1000, 5));
        wvStartSpinner.setToolTipText("In nanometers. The wavelength to start scanning at.");

        wvStopSpinner = new JSpinner(new SpinnerNumberModel(700, 400, 1000, 5));
        wvStopSpinner.setToolTipText("In nanometers. The wavelength to stop scanning at.");

        wvStepSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
        
        ttlTriggerCheckbox.setToolTipText("Whether the camera should be configured to trigger wavelength changes in the filter over TTL. This may not be supported.");
        ttlTriggerCheckbox.addActionListener((evt)->{
            boolean checked = ttlTriggerCheckbox.isSelected();
            if (!checked) {
                externalTriggerCheckBox.setSelected(false);
            }
            externalTriggerCheckBox.setEnabled(checked);
        });
                
        
        externalTriggerCheckBox.setToolTipText("Whether the filter should trigger a new camera acquisition over TTL. This is not possible for LCTF but can be done with the VF-5 Filter.");
        externalTriggerCheckBox.setEnabled(false);
        
        this.addDocumentChangeListeners(new JComponent[] {exposureSpinner, wvStartSpinner, wvStepSpinner, wvStepSpinner, ttlTriggerCheckbox, externalTriggerCheckBox});
        
        super.add(new JLabel("Start (nm"));
        super.add(new JLabel("Stop (nm)"));
        super.add(new JLabel("Step (nm)"));
        super.add(new JLabel("Exposure (ms)"), "wrap");
        super.add(wvStartSpinner);
        super.add(wvStopSpinner);
        super.add(wvStepSpinner);
        super.add(exposureSpinner, "wrap");
        super.add(ttlTriggerCheckbox, "wrap");
        super.add(externalTriggerCheckBox, "wrap");


    }
        
}
