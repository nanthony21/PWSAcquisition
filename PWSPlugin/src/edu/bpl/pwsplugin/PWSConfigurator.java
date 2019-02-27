/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;


import java.awt.Color;
import java.io.File;
import org.micromanager.internal.utils.MMFrame;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.PropertyMaps;
import org.micromanager.PropertyMap;
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


public class PWSConfigurator extends MMFrame implements ProcessorConfigurator {

    private final Studio studio_;
    private PropertyMap settings_;
    private final LogManager log_;
    
    /**
     * Creates new form FrameAveragerControls
     */
    public PWSConfigurator(PropertyMap settings, Studio studio) {
        studio_ = studio;
        settings_ = settings;
        log_ = studio.logs();
        initComponents();
        customInitComponents();

        try {
            wvStartField.setText(String.valueOf(settings_.getInteger("start", 500)));
            wvStopField.setText(String.valueOf(settings_.getInteger("stop",700)));
            wvStepField.setText(String.valueOf(settings_.getInteger("step",2)));
            directoryText.setText(settings_.getString("savepath",""));
            hardwareSequencingCheckBox.setSelected(settings_.getBoolean("sequence",false));
            externalTriggerCheckBox.setSelected(settings_.getBoolean("externalTrigger",false));
            cellNumEdit.setText(String.valueOf(settings_.getInteger("cellNum", 1)));
        }
        catch (Exception e) {
            ReportingUtils.logError(e);
        }
        super.loadAndRestorePosition(200, 200);
    }       
    
    @Override
    public PropertyMap getSettings() {
        PropertyMap.Builder builder = PropertyMaps.builder();
        try{
            int start = Integer.parseInt(wvStartField.getText().trim());
            int stop = Integer.parseInt(wvStopField.getText().trim());
            int step = Integer.parseInt(wvStepField.getText().trim());
            ArrayList<Integer> wvList = new ArrayList<Integer>();
            for (int i = start; i <= stop; i += step) {
                wvList.add(i);
            }   
            int[] wvArr = new int[wvList.size()];
            for (int i=0; i<wvList.size(); i++) {
                wvArr[i] = wvList.get(i).intValue();
            }
            builder.putIntegerList("wv", wvArr);
            builder.putInteger("start", start);
            builder.putInteger("stop", stop);
            builder.putInteger("step", step);      
            builder.putBoolean("sequence", hardwareSequencingCheckBox.isSelected());
            builder.putBoolean("externalTrigger",externalTriggerCheckBox.isSelected());
            builder.putString("savepath", directoryText.getText());
            builder.putInteger("cellNum", Integer.parseInt(cellNumEdit.getText()));
        }
        catch(NumberFormatException e){
            log_.showMessage("A valid number was not specified.");
        }
        try{
            builder.putString("filtLabel", filterComboBox.getSelectedItem().toString());
        }
        catch(NumberFormatException e){
            log_.showMessage("A valid string was not specified.");
        }
        return builder.build();
    }
        
    @Override
    public void cleanup() {
        dispose();
    }
    
    @Override
    public void showGUI() {
        pack();
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
        setVisible(true);
    }
    
    public void settingsChanged() {
        submitButton.setBackground(Color.red);
    }
    
    public void customInitComponents() {
        javax.swing.JTextField[] fields = {wvStartField, wvStopField,
            wvStepField, directoryText,
            cellNumEdit};
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
        submitButton = new javax.swing.JButton();

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

        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(submitButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        studio_.data().notifyPipelineChanged();
    }//GEN-LAST:event_formWindowClosing

    private void wvStartFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wvStartFieldActionPerformed
        //settingsChanged();
    }//GEN-LAST:event_wvStartFieldActionPerformed

    private void wvStopFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wvStopFieldActionPerformed
        // TODO add your handling code here:
        //settingsChanged();
    }//GEN-LAST:event_wvStopFieldActionPerformed

    private void filterComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterComboBoxActionPerformed
        // TODO add your handling code here:
        settingsChanged();
    }//GEN-LAST:event_filterComboBoxActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        studio_.data().notifyPipelineChanged();
        submitButton.setBackground(Color.green);
    }//GEN-LAST:event_submitButtonActionPerformed

    private void directoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryButtonActionPerformed
        File f = FileDialogs.openDir(this, "Directory to save to",
        new FileDialogs.FileType("SaveDir", "Save Directory", "D:\\Data", true, ""));
        directoryText.setText(f.getAbsolutePath());
    }//GEN-LAST:event_directoryButtonActionPerformed

    private void hardwareSequencingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hardwareSequencingCheckBoxActionPerformed
        settingsChanged();
    }//GEN-LAST:event_hardwareSequencingCheckBoxActionPerformed

    private void cellNumEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cellNumEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cellNumEditActionPerformed

    private void externalTriggerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalTriggerCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_externalTriggerCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cellNumEdit;
    private javax.swing.JButton directoryButton;
    private javax.swing.JTextField directoryText;
    private javax.swing.JCheckBox externalTriggerCheckBox;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JCheckBox hardwareSequencingCheckBox;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel startLabel;
    private javax.swing.JLabel stepLabel;
    private javax.swing.JLabel stepLabel1;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JButton submitButton;
    private javax.swing.JTextField wvStartField;
    private javax.swing.JTextField wvStepField;
    private javax.swing.JTextField wvStopField;
    // End of variables declaration//GEN-END:variables

    //API
    public void setSavePath(String savepath) {
        directoryText.setText(savepath);
        studio_.data().notifyPipelineChanged();
    }
    
    public void setCellNumber(int cellNum) {
        cellNumEdit.setText(String.valueOf(cellNum));
        studio_.data().notifyPipelineChanged();
    }

}
