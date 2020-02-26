///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2019
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.PWSPlugin;
import edu.bpl.pwsplugin.UI.subpages.DynPanel;
import edu.bpl.pwsplugin.UI.subpages.FluorPanel;
import edu.bpl.pwsplugin.UI.subpages.HWConfPanel;
import edu.bpl.pwsplugin.UI.subpages.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.MMFrame;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.propertymap.MutablePropertyMapView;

/**
 *
 * @author Nick Anthony
 */
public class PluginFrame extends MMFrame{
    private final JTabbedPane tabs = new JTabbedPane();
    private final JButton acqDynButton = new JButton("Acquire Dynamics");
    private final JButton acqFlButton = new JButton("Acquire Fluorescence");
    private final JButton acqPwsButton = new JButton("Acquire PWS");
    private final DirectorySelector dirSelect;
    private final JSpinner cellNumSpinner;
    private final PWSPanel pwsPanel = new PWSPanel();
    private final FluorPanel flPanel = new FluorPanel();
    private final DynPanel dynPanel = new DynPanel();
    private final HWConfPanel hwPanel = new HWConfPanel();
    private final MutablePropertyMapView settings_;
    
    private PWSPluginSettings.HWConfiguration lastHWConfig;
    private PWSPluginSettings.PWSSettings lastPWSSettings;
    private PWSPluginSettings.DynSettings lastDynSettings;
    private PWSPluginSettings.FluorSettings lastFluorSettings;

    public PluginFrame() {
        super("PWS Plugin");
        super.loadAndRestorePosition(100, 100);
        super.setLayout(new MigLayout());
        super.setTitle(String.format("%s %s", PWSPlugin.menuName, PWSPlugin.versionNumber));
        super.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        super.setResizable(true);
        
        dirSelect = new DirectorySelector(DirectorySelector.DefaultMMFunctions.MMDataSetDirectory);
        cellNumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));
        ((JSpinner.DefaultEditor)cellNumSpinner.getEditor()).getTextField().setColumns(4);
        
        this.settings_ = Globals.mm().profile().getSettings(PluginFrame.class);
        this.loadSettings();
        
        acqDynButton.addActionListener((e)->{ this.acquireDynamics(); });
        acqFlButton.addActionListener((e)->{ this.acquireFluorescence(); });
        acqPwsButton.addActionListener((e)->{ this.acquirePws(); });

        super.add(tabs, "wrap, span, grow");
        tabs.addTab("PWS", this.pwsPanel);
        tabs.addTab("Fluorescence", this.flPanel);
        tabs.addTab("Dynamics", this.dynPanel);
        tabs.addTab("Config", this.hwPanel);
        
        JPanel bottomPanel = new JPanel(new MigLayout());
        bottomPanel.add(dirSelect, "grow, pushx");
        bottomPanel.add(new JLabel("Cell#:"), "shrink");
        bottomPanel.add(cellNumSpinner, "wrap");
        JPanel buttons = new JPanel(new MigLayout());
        buttons.add(acqPwsButton);
        buttons.add(acqFlButton);
        buttons.add(acqDynButton);
        bottomPanel.add(buttons, "span, align center");
        super.add(bottomPanel, "dock south");
        
        super.pack();
        this.setMinimumSize(this.getSize());
    }
    
    public PWSPluginSettings getSettings() {
        PWSPluginSettings set = new PWSPluginSettings();
        set.pwsSettings = this.pwsPanel.build();
        set.dynSettings = this.dynPanel.build();
        set.flSettings = this.flPanel.build();
        set.hwConfiguration = this.hwPanel.build();
        set.saveDir = this.dirSelect.getText();
        set.cellNum = (int) this.cellNumSpinner.getValue();
        return set;
    }
    
    public void saveSettings() {
        this.settings_.putString("settings", this.getSettings().toJsonString());
    }
    
    public final void loadSettings() {
        PWSPluginSettings set = PWSPluginSettings.fromJsonString(this.settings_.getString("settings", ""));
        if (set==null) {
            Globals.mm().logs().logMessage("PWS Plugin: no settings found in user profile.");
        } else {
            try{ this.pwsPanel.populateFields(set.pwsSettings); } catch(Exception e) {ReportingUtils.logError(e); }
            try{ this.dynPanel.populateFields(set.dynSettings); } catch(Exception e) {ReportingUtils.logError(e); }
            try{ this.flPanel.populateFields(set.flSettings); } catch(Exception e) {ReportingUtils.logError(e); }
            try{ this.hwPanel.populateFields(set.hwConfiguration); } catch(Exception e) {ReportingUtils.logError(e); }
            Globals.setHardwareConfiguration(set.hwConfiguration);
            this.dirSelect.setText(set.saveDir);
            this.cellNumSpinner.setValue(set.cellNum);
        }
    }
    
    @Override
    public void dispose() {
        this.saveSettings();
        super.dispose();
    }
    
    private SwingWorker<Void, Void> runInBackground(JButton button, Runnable myFunc) {
        //This function will run myFunc in a separate thread. `button` will be disabled while the function is running.
        return new SwingWorker<Void, Void>() {
            Object o = new Object() {{button.setEnabled(false); execute();}}; //Fake constructor.
            
            @Override
            public Void doInBackground() {myFunc.run(); return null;}

            @Override
            public void done() {button.setEnabled(true);}
        };
    }
        
    private void acquire(JButton button, Runnable f) {
        try {
            this.configureManager();
        } catch (Exception e) {
            Globals.mm().logs().showError(e);
            return;
        }
        SwingWorker worker = runInBackground(button, f);
    }
    
    private void configureManager() throws Exception {
        PWSPluginSettings.HWConfiguration config = this.hwPanel.build();
        if (!config.equals(this.lastHWConfig)) {
            this.lastHWConfig = config;
            Globals.setHardwareConfiguration(config);
        }
        PWSPluginSettings.PWSSettings pwsSettings = this.pwsPanel.build();
        if (!pwsSettings.equals(this.lastPWSSettings)) {
            this.lastPWSSettings = pwsSettings;
            Globals.acqManager().setPWSSettings(pwsSettings);
        }
        PWSPluginSettings.DynSettings dynSettings = this.dynPanel.build();
        if (!dynSettings.equals(this.lastDynSettings)) {
            this.lastDynSettings = dynSettings;
            Globals.acqManager().setDynamicsSettings(dynSettings);
        }
        PWSPluginSettings.FluorSettings fluorSettings = flPanel.build();
        if (!fluorSettings.equals(this.lastFluorSettings)) {
            this.lastFluorSettings = fluorSettings;
            Globals.acqManager().setFluorescenceSettings(fluorSettings);
        }        
        String savePath = this.dirSelect.getText();
        //TODO validate path
        Globals.acqManager().setCellNum((int) this.cellNumSpinner.getValue());
        Globals.acqManager().setSavePath(savePath);
    }
    
    //Public API
    public void acquirePws() {
        acquire(acqPwsButton, Globals.acqManager()::acquirePWS);
    }
    
    public void acquireDynamics() {
        acquire(acqDynButton, Globals.acqManager()::acquireDynamics);
    }
    
    public void acquireFluorescence() {
        acquire(acqFlButton, Globals.acqManager()::acquireFluorescence);
    }
    
    public void setSavePath(String savepath) {
        dirSelect.setText(savepath);
    }
    
    public void setCellNumber(int cellNum) {
        cellNumSpinner.setValue(cellNum);
    }
    
    public String getFilterName() {
        return this.flPanel.getSelectedFilterName();
    }
    
    public void setPWSExposure(double exposureMs) {
        this.pwsPanel.setExposure(exposureMs);
    }
    
    public void setDynamicsExposure(double exposureMs) {
        this.dynPanel.setExposure(exposureMs);
    }
    
    public void setFluorescenceExposure(double exposureMs) {
        this.flPanel.setExposure(exposureMs);
    }
    
    public void setFluorescenceFilter(String filterBlockName) {
       if (!this.getFluorescenceFilterNames().contains(filterBlockName)) {
           Globals.mm().logs().showMessage(filterBlockName + " is not a valid filter block name.");
       } else {
           boolean success = this.flPanel.setFluorescenceFilter(filterBlockName);
           if (!success) {
               Globals.mm().logs().showMessage("Error settings fluoresence filter via API.");
           }
       }
    }
    
    public List<String> getFluorescenceFilterNames() {
        return this.flPanel.getFluorescenceFilterNames();
    }
    
    public void setFluorescenceEmissionWavelength(int wv) {
        this.flPanel.setEmissionWavelength(wv);
    }
}
