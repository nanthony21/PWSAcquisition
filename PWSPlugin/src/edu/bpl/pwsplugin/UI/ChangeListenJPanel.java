/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import java.awt.LayoutManager;
import java.util.HashMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author nick
 */
public abstract class ChangeListenJPanel extends JPanel{
    private boolean stale=false;
            
    public ChangeListenJPanel(LayoutManager layout) {
        super(layout);
    }
    
    protected void addDocumentChangeListeners(JComponent[] fields) {
        for (JComponent field: fields) { 
            if (field instanceof JTextField) {
                this.addDocListeners((JTextField) field);
            } else if (field instanceof JSpinner) {
                this.addDocListeners(((JSpinner.DefaultEditor)((JSpinner) field).getEditor()).getTextField());
            } else if (field instanceof JCheckBox){
                ((JCheckBox) field).addActionListener((evt)->{ this.stale=true; });
            }
        }
    }
    
    private void addDocListeners(JTextField jt) {
        jt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                stale = true;
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                stale = true;
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                stale = true;;
            }
        });
    }
    
    public boolean isStale() { return this.stale; }
    
    //protected void settingsChanged() { this.stale = true; }
}
