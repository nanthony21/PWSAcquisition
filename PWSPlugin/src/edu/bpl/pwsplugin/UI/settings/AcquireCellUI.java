/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.CheckBoxPanel;
import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.hardware.configurations.HWConfiguration;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireCellUI extends BuilderJPanel<AcquireCellSettings> implements PropertyChangeListener {
    PWSPanel pwsSettings = new PWSPanel();
    DynPanel dynSettings = new DynPanel();
    ListCardUI<List<FluorSettings>, FluorSettings> fluorSettings= new ListCardUI(ArrayList.class, "", new FluorSettings());
    CheckBoxPanel pwsCBPanel = new CheckBoxPanel(pwsSettings, "PWS");
    CheckBoxPanel dynCBPanel = new CheckBoxPanel(dynSettings, "Dynamics");
    CheckBoxPanel fluorCBPanel = new CheckBoxPanel(fluorSettings, "Fluorescence");

    public AcquireCellUI() {
        super(new MigLayout("insets 0 0 0 0"), AcquireCellSettings.class);
        Globals.addPropertyChangeListener(this);
        
        pwsSettings.setBorder(BorderFactory.createEtchedBorder());
        dynSettings.setBorder(BorderFactory.createEtchedBorder());
        fluorSettings.setBorder(BorderFactory.createEtchedBorder());

        this.add(pwsCBPanel, "wrap, span");
        this.add(dynCBPanel, "wrap, span");
        this.add(fluorCBPanel, "wrap, span");
        
        this.setConfigNames(new ArrayList()); //We can't yet reference Globals on initialization. at least initialize an empty state. the property change listener should get fired afterward.
    }
    
    @Override
    public AcquireCellSettings build() throws BuilderPanelException {
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
            settings.fluorSettings = new ArrayList();
        }
        return settings;
    }
    
    @Override
    public void populateFields(AcquireCellSettings settings) throws BuilderPanelException {
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
        if (settings.fluorSettings.isEmpty()) {
            this.fluorCBPanel.setSelected(false);
        } else {
            this.fluorCBPanel.setSelected(true);
            this.fluorSettings.populateFields(settings.fluorSettings);
        }
    }
    
    public Map<String, Object> getPropertyFieldMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("pwsSettings", pwsSettings);
        m.put("dynSettings", dynSettings);
        m.put("fluorSettings", fluorSettings);
        return m;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //We subscribe to the Globals property changes. This gets fired when a change is detected.
        if (evt.getPropertyName().equals("config")) {
            HWConfiguration cfg = (HWConfiguration) evt.getNewValue();
            setConfigNames(cfg.getSettings().configs);
        }
    }
    
    private void setConfigNames(List<ImagingConfigurationSettings> settings) {
        List<String> normalNames = new ArrayList<>();
        List<String> spectralNames = new ArrayList<>();
        for (ImagingConfigurationSettings setting : settings) {
            if (setting.configType == ImagingConfiguration.Types.StandardCamera) {
                normalNames.add(setting.name);
            } else if (setting.configType == ImagingConfiguration.Types.SpectralCamera) {
                spectralNames.add(setting.name);
            }
        }
        this.pwsSettings.setAvailableConfigNames(spectralNames);
        this.dynSettings.setAvailableConfigNames(spectralNames);
        List<String> allNames = new ArrayList<>();
        allNames.addAll(normalNames);
        allNames.addAll(spectralNames);
        for (BuilderJPanel flSettings : fluorSettings.getSubComponents()) {
            ((FluorPanel) flSettings).setAvailableConfigNames(allNames);
        }
    }
}