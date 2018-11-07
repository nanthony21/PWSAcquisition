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
        }
        catch (Exception e) {}
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
            builder.putString("savepath", directoryText.getText());
            builder.putInteger("delayMs", Integer.parseInt(delayEdit.getText()));
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
        javax.swing.JTextField[] fields = {wvStartField, wvStopField, wvStepField, directoryText, delayEdit};
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

        jPanel1 = new javax.swing.JPanel();
        startLabel = new javax.swing.JLabel();
        wvStartField = new javax.swing.JTextField();
        stopLabel = new javax.swing.JLabel();
        wvStopField = new javax.swing.JTextField();
        stepLabel = new javax.swing.JLabel();
        wvStepField = new javax.swing.JTextField();
        filterLabel = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox<String>();
        submitButton = new javax.swing.JButton();
        directoryText = new javax.swing.JTextField();
        directoryButton = new javax.swing.JButton();
        hardwareSequencingCheckBox = new javax.swing.JCheckBox();
        delayEdit = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setAutoscrolls(true);
        jPanel1.setMinimumSize(new java.awt.Dimension(332, 142));
        jPanel1.setPreferredSize(new java.awt.Dimension(332, 142));

        startLabel.setText("Start");

        wvStartField.setText("500");
        wvStartField.setName(""); // NOI18N
        wvStartField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wvStartFieldActionPerformed(evt);
            }
        });

        stopLabel.setText("Stop");

        wvStopField.setText("700");
        wvStopField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wvStopFieldActionPerformed(evt);
            }
        });

        stepLabel.setText("Step");

        wvStepField.setText("2");

        filterLabel.setText("Filter");

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        filterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterComboBoxActionPerformed(evt);
            }
        });

        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        directoryButton.setText("...");
        directoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryButtonActionPerformed(evt);
            }
        });

        hardwareSequencingCheckBox.setLabel("Use Hardware Sequencing");
        hardwareSequencingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hardwareSequencingCheckBoxActionPerformed(evt);
            }
        });

        delayEdit.setText("jTextField1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(directoryText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(directoryButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(submitButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hardwareSequencingCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(delayEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(startLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wvStartField, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(stopLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wvStopField, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(stepLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(wvStepField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(filterLabel)
                        .addGap(18, 18, 18)
                        .addComponent(filterComboBox, 0, 97, Short.MAX_VALUE)))
                .addGap(6, 6, 6))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(wvStartField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(startLabel)
                        .addComponent(stopLabel)
                        .addComponent(wvStopField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(stepLabel)
                        .addComponent(wvStepField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(filterLabel))
                    .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryButton)
                    .addComponent(directoryText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(hardwareSequencingCheckBox)
                        .addComponent(delayEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(submitButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField delayEdit;
    private javax.swing.JButton directoryButton;
    private javax.swing.JTextField directoryText;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JCheckBox hardwareSequencingCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel startLabel;
    private javax.swing.JLabel stepLabel;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JButton submitButton;
    private javax.swing.JTextField wvStartField;
    private javax.swing.JTextField wvStepField;
    private javax.swing.JTextField wvStopField;
    // End of variables declaration//GEN-END:variables
}
