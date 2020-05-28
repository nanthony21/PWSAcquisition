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
import edu.bpl.pwsplugin.UI.settings.AcquireCellUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.SequencerUI;
import edu.bpl.pwsplugin.UI.settings.HWConfPanel;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.ThrowingFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.awt.Window;
import java.util.function.Function;
import javax.swing.BorderFactory;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.MMFrame;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony
 */
public class PluginFrame extends MMFrame {
    private final JTabbedPane tabs = new JTabbedPane();
    private final AcquisitionPanel acqPanel = new AcquisitionPanel();
    private final SequencerUI sequencePanel = new SequencerUI();
    private final ConfDialog configDialog = new ConfDialog(this);

    public PluginFrame() {
        super("PWS Plugin");
        this.loadAndRestorePosition(100, 100);
        this.setLayout(new MigLayout());
        this.setTitle(String.format("%s %s", PWSPlugin.menuName, PWSPlugin.versionNumber));
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        
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

        this.add(tabs, "wrap, span, grow");
        tabs.addTab("Sequencing", this.sequencePanel);
        tabs.addTab("Quick Acquire", this.acqPanel);
        
        this.pack();
        this.setMinimumSize(this.getSize());
    }
    
    public PWSPluginSettings getSettings() {
        PWSPluginSettings set = new PWSPluginSettings();
        try {
            set.acquisitionSettings = this.acqPanel.getAcqSettings();
        } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.logError(e);
            ReportingUtils.showError("Failed to get acquisition settings from UI. See CoreLog for details.");
        }
        set.hwConfiguration = this.configDialog.build();
        set.saveDir = this.acqPanel.getDirectory();
        set.cellNum = this.acqPanel.getCellNumber();
        try {
            set.sequenceRoot = this.sequencePanel.build();
        } catch (BuilderJPanel.BuilderPanelException e) {
            set.sequenceRoot = (ContainerStep) new RootStepFactory().createStep();
        }
        return set;
    }
    
    @Override
    public void dispose() {
        Globals.saveSettings(this.getSettings());
        super.dispose();
    }
    
    public final void populateFields(PWSPluginSettings set) {
        try{ this.acqPanel.setAcqSettings(set.acquisitionSettings); } catch(NullPointerException | BuilderJPanel.BuilderPanelException e) {ReportingUtils.logError(e); } //Sometimes a bit of settings will be missing if the code is changed. Don't let that crash the program.
        try{ this.configDialog.populateFields(set.hwConfiguration); } catch(NullPointerException e) {ReportingUtils.logError(e); }
        try{ this.acqPanel.setDirectory(set.saveDir); } catch(NullPointerException e) {ReportingUtils.logError(e); }
        try{ this.acqPanel.setCellNumber(set.cellNum); } catch(NullPointerException e) {ReportingUtils.logError(e); }
        try{ this.sequencePanel.populateFields(set.sequenceRoot); } catch(NullPointerException e) {ReportingUtils.logError(e); }
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

class AcquisitionPanel extends JPanel {
    private final JButton acqButton = new JButton("Acquire Now");
    private final DirectorySelector dirSelect = new DirectorySelector(DirectorySelector.DefaultMMFunctions.MMDataSetDirectory);;
    private final JSpinner cellNumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));
    private final AcquireCellUI cellUI = new AcquireCellUI();
    
    public AcquisitionPanel() {
        super(new MigLayout("insets 0 0 0 0"));
        
        ((JSpinner.DefaultEditor)cellNumSpinner.getEditor()).getTextField().setColumns(4);
        
        acqButton.addActionListener((e)->{
            this.acquire();
        });
        
        this.add(cellUI, "spanx, wrap, shrinky, top");
        JPanel dirPanel = new JPanel(new MigLayout("insets 0 3 5 0"));
        dirPanel.add(new JLabel("Directory:"), "cell 0 0");
        dirPanel.add(dirSelect, "cell 0 0");
        dirPanel.add(acqButton, "cell 0 1");
        dirPanel.add(new JLabel("Cell#:"), "cell 0 1");
        dirPanel.add(cellNumSpinner, "cell 0 1"); 
        dirPanel.setBorder(BorderFactory.createEtchedBorder());
        this.add(dirPanel, "gapleft 5");
    }
    
    public String getDirectory() {
        return dirSelect.getText();
    }
    
    public Integer getCellNumber() {
        return (Integer) cellNumSpinner.getValue();
    }
    
    public AcquireCellSettings getAcqSettings() throws BuilderJPanel.BuilderPanelException {
        return this.cellUI.build();
    }
    
    public void setDirectory(String dir) {
        dirSelect.setText(dir);
    }
    
    public void setCellNumber(Integer num) {
        cellNumSpinner.setValue(num);
    }
    
    public void setAcqSettings(AcquireCellSettings settings) throws BuilderJPanel.BuilderPanelException {
        this.cellUI.populateFields(settings);
    }
    
    private void acquire() { 
        AcquisitionManager acqMan = Globals.acqManager();
        acqMan.setSavePath(this.dirSelect.getText());
        acqMan.setCellNum((Integer) this.cellNumSpinner.getValue());
        AcquireCellSettings settings;
        try {
            settings = this.cellUI.build();
        } catch (BuilderJPanel.BuilderPanelException e) {
            ReportingUtils.logError(e);
            ReportingUtils.showError("Failed to get acquisition settings from UI. See corelog for details.");
            return;
        }
        ThrowingFunction<Void, Void> f = (nul)->{return null;};
        for (FluorSettings flSettings : settings.fluorSettings) { //TODO check for file conflicts here
            f = f.andThen((nul)->{
                acqMan.setFluorescenceSettings(flSettings);
                acqMan.acquireFluorescence(); return null;
            });
        }
        if (settings.pwsSettings != null) {
            f = f.andThen((nul)->{
                acqMan.setPWSSettings(settings.pwsSettings);
                acqMan.acquirePWS(); return null;
            });
        }
        if (settings.dynSettings != null) {
            f = f.andThen((nul)->{
                acqMan.setDynamicsSettings(settings.dynSettings);
                acqMan.acquireDynamics(); return null;
            });
        }
        final ThrowingFunction<Void, Void> F = f;
        SwingWorker worker = new SwingWorker() {   //This function will run myFunc in a separate thread. `button` will be disabled while the function is running.
            @Override
            protected Object doInBackground() {
                try {
                    F.apply(null);
                } catch (RuntimeException e) {
                    Globals.mm().logs().showError(e); //This also logs the error.
                } finally {
                    SwingUtilities.invokeLater(()->{
                        acqButton.setEnabled(true);
                    });
                } return null; 
            }
        };   
        acqButton.setEnabled(false);
        worker.execute();           
    }
}