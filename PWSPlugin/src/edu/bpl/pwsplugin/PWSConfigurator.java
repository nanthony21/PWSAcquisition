/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;


import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import org.micromanager.internal.utils.MMFrame;
import org.micromanager.PropertyMap;
import org.micromanager.propertymap.MutablePropertyMapView;
import org.micromanager.Studio;
import org.micromanager.LogManager;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import mmcorej.StrVector;
import java.util.Arrays;
import org.micromanager.internal.utils.FileDialogs;
import org.micromanager.internal.utils.ReportingUtils;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

public class PWSConfigurator extends MMFrame {

    private final Studio studio_;
    private MutablePropertyMapView settings_;
    private final LogManager log_;
    private PWSProcessor processor_;
    private boolean settingsStale_ = true;
    Thread thread;
    
    /**
     * 
     */
    public PWSConfigurator(Studio studio) {
        studio_ = studio;
        settings_ = studio_.profile().getSettings(PWSConfigurator.class);
        log_ = studio.logs();
        
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
               saveSettings();
            }
        });
        
        initComponents();
        customInitComponents();
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
            linearityCorrectionEdit.setText(StringUtils.join(ArrayUtils.toObject(settings_.getIntegerList(PWSPlugin.linearityPolySetting)), ","));  //String.join(",",Arrays.asList(settings_.getIntegerList(PWSPlugin.linearityPolySetting)).stream().map(Object::toString).collect(Collectors.toList()))); //convert from int[] to csv string.      
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
            int[] linearityPolynomial = Arrays.asList(linearityCorrectionEdit.getText().split(","))
                                .stream()
                                .map(String::trim)
                                .mapToInt(Integer::parseInt).toArray();
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
            settings_.putIntegerList(PWSPlugin.linearityPolySetting, linearityPolynomial);
            settings_.putString(PWSPlugin.systemNameSetting, systemNameEdit.getText());
            settings_.putBoolean(PWSPlugin.sequenceSetting, hardwareSequencingCheckBox.isSelected());
            settings_.putBoolean(PWSPlugin.externalTriggerSetting,externalTriggerCheckBox.isSelected());
            settings_.putString(PWSPlugin.savePathSetting, directoryText.getText());
            settings_.putInteger(PWSPlugin.cellNumSetting, Integer.parseInt(cellNumEdit.getText()));
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
    
    private void settingsChanged() {
        settingsStale_ = true;
        submitButton.setBackground(Color.red);
    }
    
    private void customInitComponents() {
        javax.swing.JTextField[] fields = {wvStartField, wvStopField,
            wvStepField, directoryText,
            cellNumEdit, systemNameEdit, darkCountsEdit, linearityCorrectionEdit};
        for (int i=0; i<fields.length; i++) {
            fields[i].getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    settingsChanged();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    settingsChanged();
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    settingsChanged();
                }
            });
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
        wvStopField = new javax.swing.JTextField();
        wvStepField = new javax.swing.JTextField();
        wvStartField = new javax.swing.JTextField();
        stepLabel = new javax.swing.JLabel();
        stopLabel = new javax.swing.JLabel();
        startLabel = new javax.swing.JLabel();
        directoryText = new javax.swing.JTextField();
        directoryButton = new javax.swing.JButton();
        cellNumEdit = new javax.swing.JTextField();
        stepLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox<String>();
        hardwareSequencingCheckBox = new javax.swing.JCheckBox();
        externalTriggerCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        systemNameEdit = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        darkCountsEdit = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        linearityCorrectionEdit = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        submitButton = new javax.swing.JButton();
        attachButton = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        wvStopField.setText("700");
        wvStopField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wvStopFieldActionPerformed(evt);
            }
        });

        wvStepField.setText("2");

        wvStartField.setText("500");
        wvStartField.setName(""); // NOI18N
        wvStartField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wvStartFieldActionPerformed(evt);
            }
        });

        stepLabel.setText("Step");

        stopLabel.setText("Stop");

        startLabel.setText("Start");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(startLabel)
                        .addGap(27, 27, 27)
                        .addComponent(stopLabel)
                        .addGap(43, 43, 43)
                        .addComponent(stepLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(wvStartField, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(wvStopField, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(wvStepField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stopLabel)
                    .addComponent(stepLabel)
                    .addComponent(startLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wvStopField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wvStepField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wvStartField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 17, Short.MAX_VALUE))
        );

        directoryButton.setText("...");
        directoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryButtonActionPerformed(evt);
            }
        });

        cellNumEdit.setText("1");
        cellNumEdit.setToolTipText("Cell Number");
        cellNumEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cellNumEditActionPerformed(evt);
            }
        });

        stepLabel1.setText("Cell Number");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(directoryText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(directoryButton))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(stepLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cellNumEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(161, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryButton)
                    .addComponent(directoryText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cellNumEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stepLabel1))
                .addGap(0, 24, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab1", jPanel2);

        filterLabel.setText("Filter");

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        filterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterComboBoxActionPerformed(evt);
            }
        });

        hardwareSequencingCheckBox.setLabel("Use Hardware Sequencing");
        hardwareSequencingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hardwareSequencingCheckBoxActionPerformed(evt);
            }
        });

        externalTriggerCheckBox.setText("Use External Trigger");
        externalTriggerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalTriggerCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filterLabel)
                    .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(250, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hardwareSequencingCheckBox)
                    .addComponent(externalTriggerCheckBox))
                .addGap(0, 166, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(filterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(hardwareSequencingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(externalTriggerCheckBox)
                .addContainerGap())
        );

        jTabbedPane1.addTab("tab2", jPanel4);

        systemNameEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemNameEditActionPerformed(evt);
            }
        });

        jLabel1.setText("Name");

        darkCountsEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                darkCountsEditActionPerformed(evt);
            }
        });

        jLabel2.setText("Dark Counts");

        linearityCorrectionEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linearityCorrectionEditActionPerformed(evt);
            }
        });

        jLabel3.setText("Linearity Correction");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(systemNameEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(darkCountsEdit)))
                    .addComponent(jLabel3)
                    .addComponent(linearityCorrectionEdit))
                .addContainerGap(160, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(darkCountsEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemNameEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(linearityCorrectionEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("system data", jPanel1);

        submitButton.setText("Acquire");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        attachButton.setText("Attach to AcqEngine");
        attachButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 40, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(submitButton)
                .addGap(54, 54, 54)
                .addComponent(attachButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitButton)
                    .addComponent(attachButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    }//GEN-LAST:event_formWindowClosing

    private void wvStartFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wvStartFieldActionPerformed
    }//GEN-LAST:event_wvStartFieldActionPerformed

    private void wvStopFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wvStopFieldActionPerformed
    }//GEN-LAST:event_wvStopFieldActionPerformed

    private void filterComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterComboBoxActionPerformed
        settingsChanged();
    }//GEN-LAST:event_filterComboBoxActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        submitButton.setBackground(Color.green);
        acquire();
    }//GEN-LAST:event_submitButtonActionPerformed

    private void directoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryButtonActionPerformed
        File f = FileDialogs.openDir(this, "Directory to save to",
        new FileDialogs.FileType("SaveDir", "Save Directory", "D:\\Data", true, ""));
        directoryText.setText(f.getAbsolutePath());
    }//GEN-LAST:event_directoryButtonActionPerformed

    private void hardwareSequencingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hardwareSequencingCheckBoxActionPerformed
        settingsChanged();
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
        settingsChanged();
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

    private void attachButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachButtonActionPerformed
        if (attachButton.isSelected()) {
            try {
                configureProcessor();
            } catch (Exception e) {
                log_.showError(e);
                return;
            }
            studio_.acquisitions().attachRunnable(-1, -1, -1, -1, processor_);
        } else {
            studio_.acquisitions().clearRunnables();
        }
    }//GEN-LAST:event_attachButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton attachButton;
    private javax.swing.JTextField cellNumEdit;
    private javax.swing.JTextField darkCountsEdit;
    private javax.swing.JButton directoryButton;
    private javax.swing.JTextField directoryText;
    private javax.swing.JCheckBox externalTriggerCheckBox;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JCheckBox hardwareSequencingCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField linearityCorrectionEdit;
    private javax.swing.JLabel startLabel;
    private javax.swing.JLabel stepLabel;
    private javax.swing.JLabel stepLabel1;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JButton submitButton;
    private javax.swing.JTextField systemNameEdit;
    private javax.swing.JTextField wvStartField;
    private javax.swing.JTextField wvStepField;
    private javax.swing.JTextField wvStopField;
    // End of variables declaration//GEN-END:variables

    //API
    public void setSavePath(String savepath) {
        directoryText.setText(savepath);
    }
    
    public void setCellNumber(int cellNum) {
        cellNumEdit.setText(String.valueOf(cellNum));
    }

    public void acquire() {
        try {
            configureProcessor();
        } catch (Exception e) {
            log_.showError(e);
            return;
        }
        if !(thread.isAlive()) { // TODO disable button until thread dies.
            thread.start();
        }
                
    }
    
    private void configureProcessor() throws Exception {
        if ((processor_==null) || (settingsStale_)){
            saveSettings();
            processor_ = new PWSProcessor(studio_, settings_.toPropertyMap());
            thread = new Thread(processor_);
            settingsStale_ = false;
            submitButton.setBackground(Color.green);
        }
    }
}
