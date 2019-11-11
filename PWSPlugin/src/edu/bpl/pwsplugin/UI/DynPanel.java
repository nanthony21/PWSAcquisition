/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.settings.Settings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.PropertyMap;

/**
 *
 * @author nick
 */
public class DynPanel extends SingleBuilderJPanel<Settings.DynSettings>{
    private JSpinner wvSpinner = new JSpinner();
    private JSpinner framesSpinner = new JSpinner();
    private JSpinner exposureSpinner = new JSpinner();
    
    public DynPanel() {
        super(new MigLayout(), Settings.DynSettings.class);
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
    
    @Override
    public Map<String, JComponent> getPropertyFieldMap() {
        Map<String, JComponent> map = new HashMap<String, JComponent>();
        map.put("exposure", exposureSpinner);
        map.put("wavelength", wvSpinner);
        map.put("numFrames", framesSpinner);
        return map;
    }
}
