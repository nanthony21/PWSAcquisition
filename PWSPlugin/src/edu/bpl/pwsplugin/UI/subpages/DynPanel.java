/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.subpages;

import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class DynPanel extends SingleBuilderJPanel<DynSettings>{
    private JSpinner wvSpinner = new JSpinner();
    private JSpinner framesSpinner = new JSpinner();
    private JSpinner exposureSpinner = new JSpinner();
    private JTextField imConfName = new JTextField(20);

    
    public DynPanel() {
        super(new MigLayout(), DynSettings.class);
        wvSpinner.setModel(new SpinnerNumberModel(550, 400,1000, 5));
        framesSpinner.setModel(new SpinnerNumberModel(200, 1, 1000, 1));
        exposureSpinner.setModel(new SpinnerNumberModel(50, 1, 500, 5));
        
        
        super.add(new JLabel("Wavelength (nm)"));
        super.add(wvSpinner, "wrap");
        super.add(new JLabel("Exposure (ms)"));
        super.add(exposureSpinner, "wrap, growx");
        super.add(new JLabel("# of Frames"));
        super.add(framesSpinner, "wrap");
        super.add(new JLabel("Imaging Configuration"), "span");
        super.add(imConfName, "span");
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("exposure", exposureSpinner);
        map.put("wavelength", wvSpinner);
        map.put("numFrames", framesSpinner);
        map.put("imConfigName", imConfName);
        return map;
    }
    
    //API
    public void setExposure(double exposureMs) {
        this.exposureSpinner.setValue(exposureMs);
    }
}
