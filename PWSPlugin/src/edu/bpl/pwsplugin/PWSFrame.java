/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;


import java.awt.Color;
import java.io.File;
import org.micromanager.internal.utils.MMFrame;
import org.micromanager.propertymap.MutablePropertyMapView;
import org.micromanager.Studio;
import org.micromanager.LogManager;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import mmcorej.StrVector;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JTextField;
import org.micromanager.internal.utils.FileDialogs;
import org.micromanager.internal.utils.ReportingUtils;
import javax.swing.SwingWorker;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author N2-LiveCell
 */
public class PWSFrame extends MMFrame {

    private final Studio studio_;
    private MutablePropertyMapView settings_;
    private final LogManager log_;
    private AcqManager acqManager_;
    private boolean otherSettingsStale_ = true;
    private boolean PWSSettingsStale_ = true;
    private boolean saveSettingsStale_ = true;
    private boolean DYNSettingsStale_ = true;
    /**
     * 
     */
    public PWSFrame(Studio studio, AcqManager manager) {
        studio_ = studio;
        acqManager_ = manager;
        settings_ = studio_.profile().getSettings(PWSFrame.class);
        log_ = studio.logs();
        
        super.setTitle(String.format("%s %s", PWSPlugin.menuName, PWSPlugin.versionNumber));
        initComponents();
        addDocListeners();
        scanDevices();
        
        try {
            wvStartField.setText(String.valueOf(settings_.getInteger(PWSPlugin.startSetting, 500)));
            wvStopField.setText(String.valueOf(settings_.getInteger(PWSPlugin.stopSetting,700)));
            wvStepField.setText(String.valueOf(settings_.getInteger(PWSPlugin.stepSetting,2)));
            directoryText.setText(settings_.getString(PWSPlugin.savePathSetting,""));
            hardwareSequencingCheckBox.setSelected(settings_.getBoolean(PWSPlugin.sequenceSetting,false));
            externalTriggerCheckBox.setSelected(settings_.getBoolean(PWSPlugin.externalTriggerSetting,false));
            cellNumEdit.setText(String.valueOf(settings_.getInteger(PWSPlugin.cellNumSetting, 1)));
            systemNameEdit.setText(settings_.getString(PWSPlugin.systemNameSetting, ""));
            darkCountsEdit.setText(String.valueOf(settings_.getInteger(PWSPlugin.darkCountsSetting, 0)));
            double[] linArray = settings_.getDoubleList(PWSPlugin.linearityPolySetting);
            if (linArray.length > 0) {
                linearityCorrectionEdit.setText(StringUtils.join(ArrayUtils.toObject(linArray), ","));
            } else {
                linearityCorrectionEdit.setText("null");
            }
            exposureEdit.setText(String.valueOf(settings_.getDouble(PWSPlugin.exposureSetting, 100.0)));
            dynExposureEdit.setText(String.valueOf(settings_.getDouble(PWSPlugin.dynExposureSetting, 50.0)));
            dynFramesEdit.setText(String.valueOf(settings_.getInteger(PWSPlugin.dynNumFramesSetting, 200)));
            dynWvEdit.setText(String.valueOf(settings_.getInteger(PWSPlugin.dynWavelengthSetting, 550)));
      
            //Do this last in case the filter is not available
            filterComboBox.setSelectedItem(settings_.getString(PWSPlugin.filterLabelSetting, ""));
        }
        catch (Exception e) {
            ReportingUtils.logError(e);
        }
        super.loadAndRestorePosition(200, 200);
    }       
    
    private void saveSettings() {
        try{
            int start = Integer.parseInt(wvStartField.getText().trim());
            int stop = Integer.parseInt(wvStopField.getText().trim());
            int step = Integer.parseInt(wvStepField.getText().trim());
            int darkCounts = Integer.parseInt(darkCountsEdit.getText().trim());
            String linText = linearityCorrectionEdit.getText().trim();
            double[] linearityPolynomial;
            if ((linText.equals("None")) || (linText.equals("null"))) {
                linearityPolynomial = null;
            } else {
                linearityPolynomial = Arrays.asList(linText.split(","))
                                .stream()
                                .map(String::trim)
                                .mapToDouble(Double::parseDouble).toArray();
            }
            ArrayList<Integer> wvList = new ArrayList<Integer>();
            for (int i = start; i <= stop; i += step) {
                wvList.add(i);
            }   
            int[] wvArr = new int[wvList.size()];
            for (int i=0; i<wvList.size(); i++) {
                wvArr[i] = wvList.get(i).intValue();
            }
            settings_.putIntegerList(PWSPlugin.wvSetting, wvArr);
            settings_.putInteger(PWSPlugin.startSetting, start);
            settings_.putInteger(PWSPlugin.stopSetting, stop);
            settings_.putInteger(PWSPlugin.stepSetting, step);    
            settings_.putInteger(PWSPlugin.darkCountsSetting, darkCounts);
            settings_.putDoubleList(PWSPlugin.linearityPolySetting, linearityPolynomial);
            settings_.putString(PWSPlugin.systemNameSetting, systemNameEdit.getText());
            settings_.putBoolean(PWSPlugin.sequenceSetting, hardwareSequencingCheckBox.isSelected());
            settings_.putBoolean(PWSPlugin.externalTriggerSetting,externalTriggerCheckBox.isSelected());
            settings_.putString(PWSPlugin.savePathSetting, directoryText.getText());
            settings_.putInteger(PWSPlugin.cellNumSetting, Integer.parseInt(cellNumEdit.getText()));
            settings_.putDouble(PWSPlugin.exposureSetting, Double.parseDouble(exposureEdit.getText()));
            settings_.putDouble(PWSPlugin.dynExposureSetting, Double.parseDouble(dynExposureEdit.getText()));
            settings_.putInteger(PWSPlugin.dynNumFramesSetting, Integer.parseInt(dynFramesEdit.getText()));
            settings_.putInteger(PWSPlugin.dynWavelengthSetting, Integer.parseInt(dynWvEdit.getText()));
        }
        catch(NumberFormatException e){
            log_.showMessage("A valid number was not specified.");
        }
        try{
            settings_.putString(PWSPlugin.filterLabelSetting, filterComboBox.getSelectedItem().toString());
        }
        catch(NumberFormatException e){
            log_.showMessage("A valid string was not specified.");
        }
        catch (Exception e){
            log_.logError(e);
        }
    }
    
    @Override
    public void dispose() {
        saveSettings();
        super.dispose();
    }
    
    private void scanDevices() {
        String[] devs = studio_.core().getLoadedDevices().toArray();
        StrVector newDevs = new StrVector();
        for (int i = 0; i < devs.length; i++) {
            try {
                if (studio_.core().isPropertySequenceable(devs[i], "Wavelength")) {
                    newDevs.add(devs[i]);
                }
            }
            catch (Exception ex) {}
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(newDevs.toArray());
        filterComboBox.setModel(model); //Update the available names.
        String oldName = settings_.getString("filtLabel","");
            if (Arrays.asList(newDevs.toArray()).contains(oldName)) {
                filterComboBox.setSelectedItem(oldName);
            }
    }
    
    private void otherSettingsChanged() {
        otherSettingsStale_ = true;
        acqPWSButton.setBackground(Color.red);
    }
    
    private void PWSSettingsChanged() {
        PWSSettingsStale_ = true;
        acqPWSButton.setBackground(Color.red);
    }
        
    private void saveSettingsChanged() {
        saveSettingsStale_ = true;
        acqPWSButton.setBackground(Color.red);
    }
    
    private void DYNSettingsChanged() {
        DYNSettingsStale_ = true;
        acqPWSButton.setBackground(Color.red);
    }
    
    private void addDocListeners() {
        HashMap<String, JTextField[]> categories = new HashMap<String, JTextField[]>();
        categories.put("other", new JTextField[] {systemNameEdit, darkCountsEdit, linearityCorrectionEdit});
        categories.put("PWS", new JTextField[] {wvStartField, wvStopField, wvStepField, exposureEdit});
        categories.put("DYN", new JTextField[] {dynExposureEdit, dynFramesEdit, dynWvEdit});
        categories.put("save", new JTextField[] {cellNumEdit, directoryText});
        
        HashMap<String, Runnable> funcs = new HashMap<String, Runnable>();
        funcs.put("other", this::otherSettingsChanged);
        funcs.put("PWS", this::PWSSettingsChanged);
        funcs.put("DYN", this::DYNSettingsChanged);
        funcs.put("save", this::saveSettingsChanged);
        
        for (HashMap.Entry<String, JTextField[]> entry : categories.entrySet()) {
            String category = entry.getKey();
            JTextField[] fields = entry.getValue();
            for (int i=0; i<fields.length; i++) {
                fields[i].getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        funcs.get(category).run();//settingsChanged();
                    }
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        funcs.get(category).run();
                    }
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        funcs.get(category).run();
                    }
                });
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        startLabel = new javax.swing.JLabel();
        stopLabel = new javax.swing.JLabel();
        stepLabel = new javax.swing.JLabel();
        stepLabel2 = new javax.swing.JLabel();
        wvStartField = new javax.swing.JTextField();
        wvStopField = new javax.swing.JTextField();
        wvStepField = new javax.swing.JTextField();
        exposureEdit = new javax.swing.JTextField();
        hardwareSequencingCheckBox = new javax.swing.JCheckBox();
        externalTriggerCheckBox = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        dynWvEdit = new javax.swing.JTextField();
        stepLabel3 = new javax.swing.JLabel();
        stepLabel4 = new javax.swing.JLabel();
        dynExposureEdit = new javax.swing.JTextField();
        dynFramesEdit = new javax.swing.JTextField();
        stepLabel5 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        cellNumEdit = new javax.swing.JTextField();
        stepLabel1 = new javax.swing.JLabel();
        directoryText = new javax.swing.JTextField();
        directoryButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox<String>();
        jPanel1 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        darkCountsEdit = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        systemNameEdit = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        linearityCorrectionEdit = new javax.swing.JTextField();
        acqPWSButton = new javax.swing.JButton();
        acqDynButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel3.setLayout(new java.awt.GridLayout(2, 5, 5, 0));

        startLabel.setText("Start (nm)");
        jPanel3.add(startLabel);

        stopLabel.setText("Stop (nm)");
        jPanel3.add(stopLabel);

        stepLabel.setText("Step (nm)");
        jPanel3.add(stepLabel);

        stepLabel2.setText("Exposure (ms)");
        jPanel3.add(stepLabel2);

        wvStartField.setText("500");
        wvStartField.setToolTipText("In nanometers. The wavelength to start scanning at.");
        wvStartField.setName(""); // NOI18N
        wvStartField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wvStartFieldActionPerformed(evt);
            }
        });
        jPanel3.add(wvStartField);

        wvStopField.setText("700");
        wvStopField.setToolTipText("In nanometers. The wavelength to stop scanning at.");
        wvStopField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wvStopFieldActionPerformed(evt);
            }
        });
        jPanel3.add(wvStopField);

        wvStepField.setText("2");
        jPanel3.add(wvStepField);

        exposureEdit.setText("100");
        exposureEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exposureEditActionPerformed(evt);
            }
        });
        jPanel3.add(exposureEdit);

        hardwareSequencingCheckBox.setToolTipText("Whether the camera should be configured to trigger wavelength changes in the filter over TTL. This may not be supported.");
        hardwareSequencingCheckBox.setLabel("Use Hardware Sequencing");
        hardwareSequencingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hardwareSequencingCheckBoxActionPerformed(evt);
            }
        });

        externalTriggerCheckBox.setText("Use External Trigger");
        externalTriggerCheckBox.setToolTipText("Whether the filter should trigger a new camera acquisition over TTL. This is not possible for LCTF but can be done with the VF-5 Filter.");
        externalTriggerCheckBox.setEnabled(false);
        externalTriggerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalTriggerCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(externalTriggerCheckBox)
                    .addComponent(hardwareSequencingCheckBox)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(hardwareSequencingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(externalTriggerCheckBox)
                .addGap(0, 60, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("PWS", jPanel2);

        dynWvEdit.setText("550");

        stepLabel3.setText("Wavelength (nm)");

        stepLabel4.setText("Exposure (ms)");

        dynExposureEdit.setText("100");
        dynExposureEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dynExposureEditActionPerformed(evt);
            }
        });

        dynFramesEdit.setText("200");
        dynFramesEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dynFramesEditActionPerformed(evt);
            }
        });

        stepLabel5.setText("# of Frames");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(stepLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stepLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(dynWvEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dynExposureEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stepLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dynFramesEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(103, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(stepLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dynFramesEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(stepLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(stepLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dynWvEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dynExposureEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(99, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Dynamics", jPanel10);

        cellNumEdit.setText("1");
        cellNumEdit.setToolTipText("Cell Number");
        cellNumEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cellNumEditActionPerformed(evt);
            }
        });

        stepLabel1.setText("Cell Number");

        directoryButton.setText("...");
        directoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(directoryText, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(directoryButton))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(stepLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cellNumEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryButton)
                    .addComponent(directoryText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cellNumEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stepLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Save Path", jPanel9);

        jPanel5.setLayout(new java.awt.GridLayout(2, 2));

        filterLabel.setText("Filter");
        jPanel5.add(filterLabel);

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        filterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterComboBoxActionPerformed(evt);
            }
        });
        jPanel5.add(filterComboBox);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(348, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(124, 124, 124))
        );

        jTabbedPane1.addTab("Hardware", jPanel4);

        jPanel8.setLayout(new java.awt.GridLayout(2, 1));

        jPanel7.setLayout(new java.awt.GridLayout(2, 2));

        jLabel2.setText("Dark Counts");
        jLabel2.setToolTipText("# of counts per pixel when the camera is not exposed to any light. E.g if measuring dark counts with 2x2 binning the number here should be 1/4 of your measurement 2x2 binning pools 4 pixels.");
        jPanel7.add(jLabel2);

        darkCountsEdit.setToolTipText("# of counts per pixel when the camera is not exposed to any light. E.g if measuring dark counts with 2x2 binning the number here should be 1/4 of your measurement 2x2 binning pools 4 pixels.");
        darkCountsEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                darkCountsEditActionPerformed(evt);
            }
        });
        jPanel7.add(darkCountsEdit);

        jLabel1.setText("Name");
        jLabel1.setToolTipText("The name of the system.");
        jPanel7.add(jLabel1);

        systemNameEdit.setToolTipText("The name of the system.");
        systemNameEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemNameEditActionPerformed(evt);
            }
        });
        jPanel7.add(systemNameEdit);

        jPanel8.add(jPanel7);

        jPanel6.setLayout(new java.awt.GridLayout(2, 1));

        jLabel3.setText("Linearity Correction");
        jLabel3.setToolTipText("Comma separated values representing the polynomial to linearize the counts from the camera. In the form \"A,B,C\" = Ax + Bx^2 + Cx^3. Type \"None\" or \"null\" if correction is not needed.");
        jPanel6.add(jLabel3);

        linearityCorrectionEdit.setToolTipText("Comma separated values representing the polynomial to linearize the counts from the camera. In the form \"A,B,C\" = Ax + Bx^2 + Cx^3. Type \"None\" or \"null\" if correction is not needed.");
        linearityCorrectionEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linearityCorrectionEditActionPerformed(evt);
            }
        });
        jPanel6.add(linearityCorrectionEdit);

        jPanel8.add(jPanel6);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(235, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("System Data", jPanel1);

        acqPWSButton.setText("Acquire PWS");
        acqPWSButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acqPWSButtonActionPerformed(evt);
            }
        });

        acqDynButton.setText("Acquire Dynamics");
        acqDynButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acqDynButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(acqPWSButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(acqDynButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(acqPWSButton)
                    .addComponent(acqDynButton)))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("General");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    }//GEN-LAST:event_formWindowClosing

    private void wvStartFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wvStartFieldActionPerformed
    }//GEN-LAST:event_wvStartFieldActionPerformed

    private void wvStopFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wvStopFieldActionPerformed
    }//GEN-LAST:event_wvStopFieldActionPerformed

    private void filterComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterComboBoxActionPerformed
        PWSSettingsChanged();
    }//GEN-LAST:event_filterComboBoxActionPerformed

    private void acqPWSButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acqPWSButtonActionPerformed
        acqPWSButton.setBackground(Color.green);
        acquirePWS();
    }//GEN-LAST:event_acqPWSButtonActionPerformed

    private void directoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryButtonActionPerformed
        File f = FileDialogs.openDir(this, "Directory to save to",
        new FileDialogs.FileType("SaveDir", "Save Directory", "D:\\Data", true, ""));
        directoryText.setText(f.getAbsolutePath());
    }//GEN-LAST:event_directoryButtonActionPerformed

    private void hardwareSequencingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hardwareSequencingCheckBoxActionPerformed
        PWSSettingsChanged();
        boolean checked = hardwareSequencingCheckBox.isSelected();
        if (!checked) {
            externalTriggerCheckBox.setSelected(false);
        }
        externalTriggerCheckBox.setEnabled(checked);
    }//GEN-LAST:event_hardwareSequencingCheckBoxActionPerformed

    private void cellNumEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cellNumEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cellNumEditActionPerformed

    private void externalTriggerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalTriggerCheckBoxActionPerformed
        PWSSettingsChanged();
    }//GEN-LAST:event_externalTriggerCheckBoxActionPerformed

    private void systemNameEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemNameEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_systemNameEditActionPerformed

    private void darkCountsEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_darkCountsEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_darkCountsEditActionPerformed

    private void linearityCorrectionEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linearityCorrectionEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_linearityCorrectionEditActionPerformed

    private void exposureEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exposureEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_exposureEditActionPerformed

    private void acqDynButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acqDynButtonActionPerformed
        acqPWSButton.setBackground(Color.green);
        acquireDynamics();
    }//GEN-LAST:event_acqDynButtonActionPerformed

    private void dynExposureEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dynExposureEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dynExposureEditActionPerformed

    private void dynFramesEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dynFramesEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dynFramesEditActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acqDynButton;
    private javax.swing.JButton acqPWSButton;
    private javax.swing.JTextField cellNumEdit;
    private javax.swing.JTextField darkCountsEdit;
    private javax.swing.JButton directoryButton;
    private javax.swing.JTextField directoryText;
    private javax.swing.JTextField dynExposureEdit;
    private javax.swing.JTextField dynFramesEdit;
    private javax.swing.JTextField dynWvEdit;
    private javax.swing.JTextField exposureEdit;
    private javax.swing.JCheckBox externalTriggerCheckBox;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JCheckBox hardwareSequencingCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField linearityCorrectionEdit;
    private javax.swing.JLabel startLabel;
    private javax.swing.JLabel stepLabel;
    private javax.swing.JLabel stepLabel1;
    private javax.swing.JLabel stepLabel2;
    private javax.swing.JLabel stepLabel3;
    private javax.swing.JLabel stepLabel4;
    private javax.swing.JLabel stepLabel5;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JTextField systemNameEdit;
    private javax.swing.JTextField wvStartField;
    private javax.swing.JTextField wvStepField;
    private javax.swing.JTextField wvStopField;
    // End of variables declaration//GEN-END:variables

    public void acquirePWS() {
        try {
            configureManager();
        } catch (Exception e) {
            log_.showError(e);
            return;
        }
        PWSBackgroundWorker worker = new PWSBackgroundWorker();
    }
    
    public void acquireDynamics() {
        try {
            configureManager();
        } catch (Exception e) {
            log_.showError(e);
            return;
        }
        DYNBackgroundWorker worker = new DYNBackgroundWorker();
    }
    
    private void configureManager() throws Exception {
        if (otherSettingsStale_ || PWSSettingsStale_ || saveSettingsStale_ || DYNSettingsStale_){
            saveSettings(); 
            if (saveSettingsStale_) {
                int cellNum = settings_.getInteger(PWSPlugin.cellNumSetting,1);
                String savePath = settings_.getString(PWSPlugin.savePathSetting, "");
                acqManager_.setCellNum(cellNum);
                acqManager_.setSavePath(savePath);
                saveSettingsStale_ = false;
            }
            if (otherSettingsStale_) {      
                int darkCounts = settings_.getInteger(PWSPlugin.darkCountsSetting,0);
                double[] linearityPolynomial = settings_.getDoubleList(PWSPlugin.linearityPolySetting);
                String systemName = settings_.getString(PWSPlugin.systemNameSetting, "");
                acqManager_.setSystemSettings(darkCounts, linearityPolynomial, systemName);
                otherSettingsStale_ = false;
            }
            if (PWSSettingsStale_) {
                int[] wv = settings_.getIntegerList(PWSPlugin.wvSetting);
                String filtLabel = settings_.getString(PWSPlugin.filterLabelSetting, "");
                boolean hardwareSequence = settings_.getBoolean(PWSPlugin.sequenceSetting, false);
                boolean useExternalTrigger = settings_.getBoolean(PWSPlugin.externalTriggerSetting, false);
                double exposure = settings_.getDouble(PWSPlugin.exposureSetting, 100);
                acqManager_.setPWSSettings(exposure, useExternalTrigger, hardwareSequence, wv, filtLabel);
                PWSSettingsStale_ = false;
            }        
            if (DYNSettingsStale_) {
                double exposure = settings_.getDouble(PWSPlugin.dynExposureSetting, 100);
                String filterLabel = settings_.getString(PWSPlugin.filterLabelSetting, "");
                int wavelength = settings_.getInteger(PWSPlugin.dynWavelengthSetting, 550);
                int numFrames = settings_.getInteger(PWSPlugin.dynNumFramesSetting, 200);
                acqManager_.setDynamicsSettings(exposure, filterLabel, wavelength, numFrames);
                DYNSettingsStale_ = false;
            }
            acqPWSButton.setBackground(Color.green);
        }
    }
        
    protected class PWSBackgroundWorker extends SwingWorker<Void, Void> {
        public PWSBackgroundWorker() {
            acqPWSButton.setEnabled(false);            
            this.execute();
        }
        
        @Override
        public Void doInBackground() {
            acqManager_.acquirePWS();
            return null;
        }

        @Override
        public void done() {
            acqPWSButton.setEnabled(true);
        }
    }
    
    protected class DYNBackgroundWorker extends SwingWorker<Void, Void> {
        public DYNBackgroundWorker() {
            acqDynButton.setEnabled(false);
            this.execute();
        }
              
        @Override
        public Void doInBackground() {
            acqManager_.acquireDynamics();
            return null;
        }

        @Override
        public void done() {
            acqDynButton.setEnabled(true);
        }
    }
    
    //API
    public void setSavePath(String savepath) {
        directoryText.setText(savepath);
    }
    
    public void setCellNumber(int cellNum) {
        cellNumEdit.setText(String.valueOf(cellNum));
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
}