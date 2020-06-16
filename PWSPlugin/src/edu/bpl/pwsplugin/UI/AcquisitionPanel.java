/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.settings.AcquireCellUI;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.ThrowingFunction;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
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
        Path savePath = FileSpecs.getCellFolderName(Paths.get(this.dirSelect.getText()), (Integer) this.cellNumSpinner.getValue());
        if (Files.exists(savePath)) {
            int option = JOptionPane.showConfirmDialog(Globals.frame(), "File already exists. Replace?", "Overwrite?", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try {
                    FileUtils.deleteDirectory(savePath.toFile());
                } catch (IOException e) {
                    Globals.mm().logs().logError(e);
                    return;
                }
            } else {
                return;
            }
        }
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
        if (!settings.fluorSettings.isEmpty()) {
            f = f.andThen((nul)->{
                acqMan.setFluorescenceSettings(settings.fluorSettings);
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
