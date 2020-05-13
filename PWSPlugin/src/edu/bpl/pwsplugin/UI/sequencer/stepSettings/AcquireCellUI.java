/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.stepSettings;

import edu.bpl.pwsplugin.UI.settings.DynPanel;
import edu.bpl.pwsplugin.UI.settings.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class AcquireCellUI extends SingleBuilderJPanel<AcquireCellSettings> {
    JTextField directory = new JTextField(10);
    PWSPanel pwsSettings = new PWSPanel();
    DynPanel dynSettings = new DynPanel();
    ListCardUI<List<FluorSettings>, FluorSettings> fluorSettings= new ListCardUI<>(ArrayList.class, "message", new FluorSettings());
    public AcquireCellUI() {
        super(new MigLayout(), AcquireCellSettings.class);
        
        this.add(directory, "wrap");
        this.add(pwsSettings, "wrap");
        this.add(dynSettings, "wrap");
        this.add(fluorSettings, "wrap");
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("pwsSettings", pwsSettings);
        m.put("dynSettings", dynSettings);
        m.put("fluorSettings", fluorSettings);
        m.put("directory", directory);
        return m;
    }
}
