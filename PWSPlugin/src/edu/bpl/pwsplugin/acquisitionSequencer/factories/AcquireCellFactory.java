/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.settings.DynPanel;
import edu.bpl.pwsplugin.UI.settings.FluorPanel;
import edu.bpl.pwsplugin.UI.settings.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.CheckBoxPanel;
import edu.bpl.pwsplugin.UI.utils.ListCardUI;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.HWConfiguration;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireCellFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return AcquireCellUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.AcquireCellSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return AcquireCell.class;
    }
    
    @Override
    public String getDescription() {
        return "Acquire PWS, Dynamics, and Fluorescence into a single folder.";
    }
    
    @Override
    public String getName() {
        return "Acquisition";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.ACQ;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.ACQ;
    }
}

class AcquireCellUI extends BuilderJPanel<SequencerSettings.AcquireCellSettings> implements PropertyChangeListener {
    PWSPanel pwsSettings = new PWSPanel();
    DynPanel dynSettings = new DynPanel();
    ListCardUI<List<FluorSettings>, FluorSettings> fluorSettings= new ListCardUI<>(ArrayList.class, "", new FluorSettings());
    CheckBoxPanel pwsCBPanel = new CheckBoxPanel(pwsSettings, "PWS");
    CheckBoxPanel dynCBPanel = new CheckBoxPanel(dynSettings, "Dynamics");
    CheckBoxPanel fluorCBPanel = new CheckBoxPanel(fluorSettings, "Fluorescence");

    public AcquireCellUI() {
        super(new MigLayout(), SequencerSettings.AcquireCellSettings.class);
        Globals.addPropertyChangeListener(this);
        
        pwsSettings.setBorder(BorderFactory.createEtchedBorder());
        dynSettings.setBorder(BorderFactory.createEtchedBorder());
        fluorSettings.setBorder(BorderFactory.createEtchedBorder());

        this.add(pwsCBPanel, "wrap, span");
        this.add(dynCBPanel, "wrap, span");
        this.add(fluorCBPanel, "wrap, span");
    }
    
    @Override
    public SequencerSettings.AcquireCellSettings build() {
        SequencerSettings.AcquireCellSettings settings = new SequencerSettings.AcquireCellSettings();
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
        return settings;
    }
    
    @Override
    public void populateFields(SequencerSettings.AcquireCellSettings settings) {
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
            List<String> normalNames = new ArrayList<>();
            List<String> spectralNames = new ArrayList<>();
            for (ImagingConfigurationSettings setting : cfg.getSettings().configs) {
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
}

class AcquireCell extends EndpointStep {
    //Represents the acquisition of a single "CellXXX" folder, it can contain multiple PWS, Dynamics, and Fluorescence acquisitions.
    public AcquireCell() {
        super(Consts.Type.ACQ);
    }
    
    @Override
    public SequencerFunction getFunction() {
        SequencerSettings.AcquireCellSettings settings = (SequencerSettings.AcquireCellSettings) this.getSettings();
        AcquisitionManager acqMan = Globals.acqManager();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception{ //TODO need to make the fluorescence not overwrite eachother.
                acqMan.setCellNum(status.currentCellNum);
                for (FluorSettings flSettings : settings.fluorSettings) {
                    status.allowPauseHere();
                    status.update(String.format("Acquiring %s fluoresence", flSettings.filterConfigName), status.currentCellNum);
                    acqMan.setFluorescenceSettings(flSettings);
                    acqMan.acquireFluorescence();
                }
                if (settings.pwsSettings != null) {
                    status.allowPauseHere();
                    status.update("Acquiring PWS", status.currentCellNum);
                    acqMan.setPWSSettings(settings.pwsSettings);
                    acqMan.acquirePWS();
                }
                if (settings.dynSettings != null) {
                    status.allowPauseHere();
                    status.update("Acquiring Dynamics", status.currentCellNum);
                    acqMan.setDynamicsSettings(settings.dynSettings);
                    acqMan.acquireDynamics();
                }
                status.allowPauseHere();
                status.currentCellNum += 1;
                return status;
            }
        };
    }
}


