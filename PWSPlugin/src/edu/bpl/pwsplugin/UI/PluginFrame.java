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
import edu.bpl.pwsplugin.HWConfiguration;
import edu.bpl.pwsplugin.PWSPlugin;
import edu.bpl.pwsplugin.UI.subpages.DynPanel;
import edu.bpl.pwsplugin.UI.subpages.FluorPanel;
import edu.bpl.pwsplugin.UI.subpages.HWConfPanel;
import edu.bpl.pwsplugin.UI.subpages.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.MMFrame;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony
 */
public class PluginFrame extends MMFrame implements PropertyChangeListener{
    private final JTabbedPane tabs = new JTabbedPane();
    private final JButton acqDynButton = new JButton("Acquire Dynamics");
    private final JButton acqFlButton = new JButton("Acquire Fluorescence");
    private final JButton acqPwsButton = new JButton("Acquire PWS");
    private final DirectorySelector dirSelect;
    private final JSpinner cellNumSpinner;
    private final PWSPanel pwsPanel = new PWSPanel();
    private final FluorPanel flPanel = new FluorPanel();
    private final DynPanel dynPanel = new DynPanel();
    private final ConfDialog configDialog = new ConfDialog(this);
    
    private PWSSettings lastPWSSettings;
    private DynSettings lastDynSettings;
    private FluorSettings lastFluorSettings;

    public PluginFrame() {
        super("PWS Plugin");
        this.loadAndRestorePosition(100, 100);
        this.setLayout(new MigLayout());
        this.setTitle(String.format("%s %s", PWSPlugin.menuName, PWSPlugin.versionNumber));
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        
        Globals.addPropertyChangeListener(this);
        
        JMenuBar ma = new JMenuBar();
        JMenu mb = new JMenu("Advanced");
        JMenuItem mc = new JMenuItem("Configuration");
        ma.add(mb);
        mb.add(mc);
        mc.addActionListener((evt)->{
            HWConfigurationSettings newSettings = this.configDialog.showDialog();
            Globals.setHardwareConfigurationSettings(newSettings);
        });
        
        this.setJMenuBar(ma);
        
        dirSelect = new DirectorySelector(DirectorySelector.DefaultMMFunctions.MMDataSetDirectory);
        cellNumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));
        ((JSpinner.DefaultEditor)cellNumSpinner.getEditor()).getTextField().setColumns(4);
        
        
        acqDynButton.addActionListener((e)->{ this.acquireDynamics(); });
        acqFlButton.addActionListener((e)->{ this.acquireFluorescence(); });
        acqPwsButton.addActionListener((e)->{ this.acquirePws(); });

        this.add(tabs, "wrap, span, grow");
        tabs.addTab("PWS", this.pwsPanel);
        tabs.addTab("Fluorescence", this.flPanel);
        tabs.addTab("Dynamics", this.dynPanel);
        
        JPanel bottomPanel = new JPanel(new MigLayout());
        bottomPanel.add(dirSelect, "grow, pushx");
        bottomPanel.add(new JLabel("Cell#:"), "shrink");
        bottomPanel.add(cellNumSpinner, "wrap");
        JPanel buttons = new JPanel(new MigLayout());
        buttons.add(acqPwsButton);
        buttons.add(acqFlButton);
        buttons.add(acqDynButton);
        bottomPanel.add(buttons, "span, align center");
        this.add(bottomPanel, "dock south");
        
        this.pack();
        this.setMinimumSize(this.getSize());
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
            this.pwsPanel.setAvailableConfigNames(spectralNames);
            this.dynPanel.setAvailableConfigNames(spectralNames);
            List<String> allNames = new ArrayList<>();
            allNames.addAll(normalNames);
            allNames.addAll(spectralNames);
            this.flPanel.setAvailableConfigNames(allNames);            
        }
    }
    
    public PWSPluginSettings getSettings() {
        PWSPluginSettings set = new PWSPluginSettings();
        set.pwsSettings = this.pwsPanel.build();
        set.dynSettings = this.dynPanel.build();
        set.flSettings = this.flPanel.build();
        set.hwConfiguration = this.configDialog.build();
        set.saveDir = this.dirSelect.getText();
        set.cellNum = (int) this.cellNumSpinner.getValue();
        return set;
    }
    
    @Override
    public void dispose() {
        Globals.saveSettings(this.getSettings());
        super.dispose();
    }
    
    public final void populateFields(PWSPluginSettings set) {
        try{ this.pwsPanel.populateFields(set.pwsSettings); } catch(Exception e) {ReportingUtils.logError(e); }
        try{ this.dynPanel.populateFields(set.dynSettings); } catch(Exception e) {ReportingUtils.logError(e); }
        try{ this.flPanel.populateFields(set.flSettings); } catch(Exception e) {ReportingUtils.logError(e); }
        try{ this.configDialog.populateFields(set.hwConfiguration); } catch(Exception e) {ReportingUtils.logError(e); }
        try{ this.dirSelect.setText(set.saveDir); } catch(Exception e) {ReportingUtils.logError(e); }
        try{ this.cellNumSpinner.setValue(set.cellNum); } catch(Exception e) {ReportingUtils.logError(e); }
    }
        
    private void acquire(JButton button, Runnable f) {
        try {
            this.configureManager();
        } catch (Exception e) {
            Globals.mm().logs().showError(e);
            return;
        }
        SwingWorker worker = new SwingWorker() {   //This function will run myFunc in a separate thread. `button` will be disabled while the function is running.
            @Override
            protected Object doInBackground() { f.run(); return null; }
            @Override
            public void done() { button.setEnabled(true); }
        };
        
        button.setEnabled(false);
        worker.execute();           
    }
    
    private void configureManager() throws Exception {
        PWSSettings pwsSettings = this.pwsPanel.build();
        if (!pwsSettings.equals(this.lastPWSSettings)) {
            this.lastPWSSettings = pwsSettings;
            Globals.acqManager().setPWSSettings(pwsSettings);
        }
        DynSettings dynSettings = this.dynPanel.build();
        if (!dynSettings.equals(this.lastDynSettings)) {
            this.lastDynSettings = dynSettings;
            Globals.acqManager().setDynamicsSettings(dynSettings);
        }
        FluorSettings fluorSettings = flPanel.build();
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

class ConfDialog extends JDialog {
    JButton acceptButton = new JButton("Accept");
    private HWConfPanel hwc = new HWConfPanel();
    
    public ConfDialog(Window owner) {
        super(owner, "Hardware Configuration");
        this.setModal(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(owner);
        
        acceptButton.addActionListener((evt)->{
            this.setVisible(false);
            this.dispose();
        });
        
        JPanel pnl = new JPanel(new MigLayout());
        pnl.add(hwc, "wrap");
        pnl.add(acceptButton, "span, align center");
        this.setContentPane(pnl);
        this.pack();
    }
    
    public void populateFields(HWConfigurationSettings config) {
        hwc.populateFields(config);
    }
    
    public HWConfigurationSettings build() {
        return hwc.build();
    }
    
    public HWConfigurationSettings showDialog() {
        this.setVisible(true);
        return this.build();
    }
}
