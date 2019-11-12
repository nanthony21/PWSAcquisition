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
F
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.PWSPlugin;
import edu.bpl.pwsplugin.UI.subpages.DynPanel;
import edu.bpl.pwsplugin.UI.subpages.FluorPanel;
import edu.bpl.pwsplugin.UI.subpages.HWConfPanel;
import edu.bpl.pwsplugin.UI.subpages.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import org.micromanager.internal.utils.MMFrame;

/**
 *
 * @author Nick Anthony
 */
public class PluginFrame extends MMFrame{
    private JTabbedPane tabs = new JTabbedPane();
    private JButton acqDynButton = new JButton("Acquire Dynamics");
    private JButton acqFlButton = new JButton("Acquire Fluorescence");
    private JButton acqPwsButton = new JButton("Acquire PWS");
    private DirectorySelector dirSelect;
    private JSpinner cellNumSpinner;
    private PWSPanel pwsPanel = new PWSPanel();
    private FluorPanel flPanel = new FluorPanel();
    private DynPanel dynPanel = new DynPanel();
    private HWConfPanel hwPanel = new HWConfPanel();

    public PluginFrame() {
        super();
        super.setTitle(String.format("%s %s", PWSPlugin.menuName, PWSPlugin.versionNumber));
        
        acqDynButton.addActionListener((e)->{ this.acquireDynamics(); });
        acqFlButton.addActionListener((e)->{ this.acquireFluorescence(); });
        acqPwsButton.addActionListener((e)->{ this.acquirePws(); });
        
        dirSelect = new DirectorySelector(DirectorySelector.DefaultMMFunctions.MMDataSetDirectory);
        cellNumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));
        
        super.add(tabs, "wrap, span");
        tabs.addTab("PWS", this.pwsPanel);
        tabs.addTab("Fluorescence", this.flPanel);
        tabs.addTab("Dynamics", this.dynPanel);
        
        super.add(dirSelect);
        super.add(new JLabel("Cell#:"));
        super.add(cellNumSpinner);
        super.add(acqPwsButton);
        super.add(acqFlButton);
        super.add(acqDynButton);
        
        super.pack();
    }
    @Override
    public void dispose() {
        this.saveSettings();
        super.dispose();
    }
    
        private SwingWorker<Void, Void> runInBackground(JButton button, Runnable myFunc) {
        //This funciton will run myFunc in a separate thread. `button` will be disabled while the function is running.
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
            configureManager();
        } catch (Exception e) {
            Globals.mm().logs().showError(e);
            return;
        }
        SwingWorker worker = runInBackground(button, f);
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
        return filterComboBox.getSelectedItem().toString();
    }
    
    public void setPWSExposure(double exposureMs) {
        exposureEdit.setText(String.valueOf(exposureMs));
    }
    
    public void setDynamicsExposure(double exposureMs) {
        dynExposureEdit.setText(String.valueOf(exposureMs));
    }
    
    public void setFluorescenceExposure(double exposureMs) {
        flExposureEdit.setText(String.valueOf(exposureMs));
    }
    
    public void setFluorescenceFilter(String filterBlockName) {
       if (!this.getFluorescenceFilterNames().contains(filterBlockName)) {
           ReportingUtils.showMessage(filterBlockName + " is not a valid filter block name.");
       } else {
        flFilterBlockCombo.setSelectedItem(filterBlockName);
       }
    }
    
    public Vector<String> getFluorescenceFilterNames() {
        Vector<String> names = new Vector<String>();
        for (int i=0; i<flFilterBlockCombo.getItemCount(); i++) {
            names.add(flFilterBlockCombo.getItemAt(i));
        }
        return names;
    }
    
    public void setFluorescenceEmissionWavelength(int wv) {
        flWvEdit.setText(String.valueOf(wv));
    }
}
