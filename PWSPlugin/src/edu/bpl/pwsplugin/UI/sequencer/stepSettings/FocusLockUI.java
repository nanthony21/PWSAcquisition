/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.stepSettings;

import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class FocusLockUI extends SingleBuilderJPanel<FocusLockSettings>{
    JSpinner offset;
    JSpinner delay;
    private Map<String,Object> m = new HashMap<>();
    
    public FocusLockUI() {
        super(new MigLayout(), FocusLockSettings.class);
        
        offset = new JSpinner(new SpinnerNumberModel(0, -1e8, 1e8, 1));
        delay = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 30.0, 1.0));
        
        this.add(new JLabel("Z Offset"));
        this.add(offset, "wrap");
        this.add(new JLabel("Delay (s)"));
        this.add(delay);
        
        m.put("zOffset", offset);
        m.put("preDelay", delay);
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {        
        return m;
    }
}
