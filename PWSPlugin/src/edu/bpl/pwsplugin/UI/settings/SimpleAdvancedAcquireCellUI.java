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
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSSettingsConsts;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class SimpleAdvancedAcquireCellUI extends BuilderJPanel<AcquireCellSettings> {
    AcquireCellUI advancedUI = new AcquireCellUI();
    SimpleAcquireCellUI simpleUI = new SimpleAcquireCellUI();
    BuilderJPanel<AcquireCellSettings> currentTab;
    JTabbedPane tabs = new JTabbedPane();
    
    public SimpleAdvancedAcquireCellUI() {
        super(new MigLayout("insets 0 0 0 0, fill"), AcquireCellSettings.class);
        
        JPanel panel = new JPanel(new MigLayout());
        panel.add(advancedUI);
        
        tabs.add("Simple", simpleUI);
        tabs.add("Advanced", panel);
        currentTab = (BuilderJPanel<AcquireCellSettings>) tabs.getSelectedComponent();
        
        tabs.addChangeListener((evt) -> { //When tab is switched populate the new tab with the settings from the old tab.
            BuilderJPanel newTab = (BuilderJPanel) tabs.getSelectedComponent();
            try {
                newTab.populateFields(currentTab.build());
            } catch (Exception e) {
                Globals.mm().logs().logError(e);
            }
            currentTab = newTab;
        });
        
        this.add(tabs, "shrinkx");
    }
    
    @Override
    public void populateFields(AcquireCellSettings settings) throws BuilderPanelException{
        advancedUI.populateFields(settings);
        simpleUI.populateFields(settings);
    }
    
    @Override
    public AcquireCellSettings build() throws BuilderPanelException {
        BuilderJPanel<AcquireCellSettings> panel = (BuilderJPanel) tabs.getSelectedComponent();
        return panel.build();
    }
}


class SimpleAcquireCellUI extends BuilderJPanel<AcquireCellSettings> implements PropertyChangeListener {
    private AcquireCellSettings settings = new AcquireCellSettings(); //Since the UI doesn't fully represent all of the settings we need to store this in the background.
    private final SimplePWSPanel pwsSettings = new SimplePWSPanel();
    private final SimplePWSPanel dynSettings = new SimplePWSPanel();
    private final CheckBoxPanel pwsCBPanel = new CheckBoxPanel(pwsSettings, "PWS");
    private final CheckBoxPanel dynCBPanel = new CheckBoxPanel(dynSettings, "Dynamics");
    private final JButton systemDefault = new JButton("Use Defaults");
    private final ListCardUI<List<FluorSettings>, FluorSettings> fluorSettings= new ListCardUI(ArrayList.class, "", new FluorSettings());
    private final CheckBoxPanel fluorCBPanel = new CheckBoxPanel(fluorSettings, "Fluorescence");

    
    public SimpleAcquireCellUI() {
        super(new MigLayout(), AcquireCellSettings.class);
        
        this.systemDefault.addActionListener((evt) -> { //Apply default settings
            JPopupMenu menu = new JPopupMenu();
            for (PWSSettingsConsts.Systems sys : PWSSettingsConsts.Systems.values()) {
                JMenuItem item = new JMenuItem(sys.name());
                item.addActionListener((evnt) -> {
                    try {
                        this.populateFields(AcquireCellSettings.getDefaultSettings(sys));
                    } catch (Exception e) {
                        Globals.mm().logs().logError(e);
                    }
                });
                menu.add(item);
            }
            menu.show(systemDefault, 0, 0);
        });
        
        pwsSettings.setBorder(BorderFactory.createEtchedBorder());
        dynSettings.setBorder(BorderFactory.createEtchedBorder());
        fluorSettings.setBorder(BorderFactory.createEtchedBorder());
        
        pwsSettings.setExposure(settings.pwsSettings.exposure);
        dynSettings.setExposure(settings.dynSettings.exposure);
        
        this.add(systemDefault, "wrap");
        this.add(pwsCBPanel, "wrap, spanx");
        this.add(dynCBPanel, "wrap, spanx");
        this.add(fluorCBPanel, "spanx");
        
        Globals.addPropertyChangeListener(this);
        this.setConfigNames(new ArrayList()); //We can't yet reference Globals on initialization. at least initialize an empty state. the property change listener should get fired afterward.
    }
    
    @Override
    public void populateFields(AcquireCellSettings settings) throws BuilderPanelException {
        this.settings = settings;
        if (settings.pwsSettings != null) {
            pwsCBPanel.setSelected(true);
            pwsSettings.setExposure(settings.pwsSettings.exposure);
            pwsSettings.setConfigName(settings.pwsSettings.imConfigName);
        } else {
            pwsCBPanel.setSelected(false);
        }
        
        if (settings.dynSettings != null) {
            dynCBPanel.setSelected(true);
            dynSettings.setExposure(settings.dynSettings.exposure);
            dynSettings.setConfigName(settings.dynSettings.imConfigName);
        } else {
            dynCBPanel.setSelected(false);
        }
        
        if (settings.fluorSettings.isEmpty()) {
            this.fluorCBPanel.setSelected(false);
        } else {
            this.fluorCBPanel.setSelected(true);
            this.fluorSettings.populateFields(settings.fluorSettings);
        }
    }
    
    @Override
    public AcquireCellSettings build() throws BuilderPanelException {
        if (pwsCBPanel.isSelected()) {
            settings.pwsSettings.exposure = this.pwsSettings.getExposure();
            settings.pwsSettings.imConfigName = this.pwsSettings.getConfigName();
        } else {
            settings.pwsSettings = null;
        }
        
        if (dynCBPanel.isSelected()) {
            settings.dynSettings.exposure = this.dynSettings.getExposure();
            settings.dynSettings.imConfigName = this.dynSettings.getConfigName();
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


class SimplePWSPanel extends JPanel {
    private final JFormattedTextField exposure = new JFormattedTextField(NumberFormat.getNumberInstance());
    private final JComboBox<String> imConfName = new JComboBox<>();
    
    public SimplePWSPanel() {
        super(new MigLayout());
        
        exposure.setColumns(6);
        
        this.add(new JLabel("Exposure (ms):"), "gapleft push");
        this.add(exposure, "wrap");
        this.add(new JLabel("Configuration:"), "gapleft push");
        this.add(imConfName, "wrap");
    }
    
    public double getExposure() {
        return ((Number) exposure.getValue()).doubleValue();
    }
    
    public void setExposure(double exp) {
        exposure.setValue(exp);
    }
    
    public String getConfigName() {
        return (String) this.imConfName.getSelectedItem();
    }
    
    public void setConfigName(String name) {
        this.imConfName.setSelectedItem(name);
    }
    
    public void setAvailableConfigNames(List<String> names) {
        this.imConfName.removeAllItems();
        if (names.isEmpty()) {
            this.imConfName.addItem("NONE!"); //Prevent a null pointer error.
        } else {
            for (String name : names) {
                this.imConfName.addItem(name);
            }
        }
    }
}
