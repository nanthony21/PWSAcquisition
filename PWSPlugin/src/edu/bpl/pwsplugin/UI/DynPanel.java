/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class DynPanel extends ChangeListenJPanel{
    private JSpinner wvSpinner = new JSpinner();
    private JSpinner framesSpinner = new JSpinner();
    private JSpinner exposureSpinner = new JSpinner();
    
    public DynPanel() {
        super(new MigLayout());
        wvSpinner.setModel(new SpinnerNumberModel(550, 400,1000, 5));
        framesSpinner.setModel(new SpinnerNumberModel(200, 1, 1000, 1));
        exposureSpinner.setModel(new SpinnerNumberModel(50, 1, 500, 5));
        
        this.addDocumentChangeListeners(new JComponent[] {wvSpinner, framesSpinner, exposureSpinner});
        
        super.add(new JLabel("Wavelength (nm"));
        super.add(wvSpinner, "wrap");
        super.add(new JLabel("Exposure (ms)"));
        super.add(exposureSpinner, "wrap");
        super.add(new JLabel("# of Frames"));
        super.add(framesSpinner, "wrap");
    }
}
