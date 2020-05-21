/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings;

import edu.bpl.pwsplugin.UI.settings.DynPanel;
import edu.bpl.pwsplugin.UI.settings.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.CheckBoxPanel;
import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class AcquireCellUI extends BuilderJPanel<AcquireCellSettings> {
    JTextField directory = new JTextField(10);
    CheckBoxPanel pwsCBPanel = new CheckBoxPanel(new MigLayout("insets 0 0 0 0"), "PWS");
    CheckBoxPanel dynCBPanel = new CheckBoxPanel(new MigLayout("insets 0 0 0 0"), "Dynamics");
    CheckBoxPanel fluorCBPanel = new CheckBoxPanel(new MigLayout("insets 0 0 0 0"), "Fluorescence");
    PWSPanel pwsSettings = new PWSPanel();
    DynPanel dynSettings = new DynPanel();
    ListCardUI<List<FluorSettings>, FluorSettings> fluorSettings= new ListCardUI<>(ArrayList.class, "", new FluorSettings());
    public AcquireCellUI() {
        super(new MigLayout(), AcquireCellSettings.class);
        
        pwsSettings.setBorder(BorderFactory.createEtchedBorder());
        dynSettings.setBorder(BorderFactory.createEtchedBorder());
        fluorSettings.setBorder(BorderFactory.createEtchedBorder());
        
        pwsCBPanel.add(pwsSettings);
        dynCBPanel.add(dynSettings);
        fluorCBPanel.add(fluorSettings);

        this.add(new JLabel("Directory:"));
        this.add(directory, "wrap");
        this.add(pwsCBPanel, "wrap, span");
        this.add(dynCBPanel, "wrap, span");
        this.add(fluorCBPanel, "wrap, span");
    }
    
    @Override
    public AcquireCellSettings build() {
        AcquireCellSettings settings = new AcquireCellSettings();
        if (pwsCBPanel.isSelected()) {
            settings.pwsSettings = pwsSettings.build();
        } else {
            settings.pwsSettings = null;
        }
        if (dynCBPanel.isSelected()) {
            settings.dynSettings = dynSettings.build();
        } else {
            settings.dynSettings = null;
        }
        if (fluorCBPanel.isSelected()) {
            settings.fluorSettings = fluorSettings.build();
        } else {
            settings.fluorSettings = null;
        }
        settings.directory = directory.getText();
        return settings;
    }
    
    @Override
    public void populateFields(AcquireCellSettings settings) {
        if (settings.pwsSettings == null) {
            this.pwsCBPanel.setSelected(false);
        } else {
            this.pwsCBPanel.setSelected(true);
            this.pwsSettings.populateFields(settings.pwsSettings);
        }
        if (settings.dynSettings == null) {
            this.dynCBPanel.setSelected(false);
        } else {
            this.dynCBPanel.setSelected(true);
            this.dynSettings.populateFields(settings.dynSettings);
        }         
        if (settings.fluorSettings == null) {
            this.fluorCBPanel.setSelected(false);
        } else {
            this.fluorCBPanel.setSelected(true);
            this.fluorSettings.populateFields(settings.fluorSettings);
        }
        this.directory.setText(settings.directory);
    }
    
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("pwsSettings", pwsSettings);
        m.put("dynSettings", dynSettings);
        m.put("fluorSettings", fluorSettings);
        m.put("directory", directory);
        return m;
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new AcquireCellUI());
                
        f.setSize(400,400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
}
