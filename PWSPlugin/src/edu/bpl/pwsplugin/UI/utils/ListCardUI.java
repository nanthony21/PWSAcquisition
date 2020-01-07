/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.utils;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import edu.bpl.pwsplugin.utils.JsonableParam;
import edu.bpl.pwsplugin.utils.UIBuildable;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ListCardUI<S extends JsonableParam & UIBuildable> extends BuilderJPanel<List<S>> implements ItemListener {
    private JComboBox<String> combo = new JComboBox<String>();
    private JPanel cardPanel = new JPanel(new CardLayout());
    JButton addButton = new JButton("Add");
    JButton remButton = new JButton("Delete");
    private List<BuilderJPanel<S>> components = new ArrayList<BuilderJPanel<S>>();
     
    
    public ListCardUI(Class<List<S>> clazz,  String msg) {
        super(new MigLayout(), clazz);
  
        this.cardPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
        
        super.add(new JLabel(msg), "gapleft push");
        super.add(combo);
        super.add(addButton);
        super.add(remButton, "wrap");
        super.add(cardPanel, "span, wrap");
        
        combo.setEditable(false);
        combo.addItemListener(this);
        
        addButton.addActionListener((e)->{this.addStepAction();});
        remButton.addActionListener((e)->{this.removeStepAction();});
    }
    
    
    @Override
    public void itemStateChanged(ItemEvent evt) {
        CardLayout cl = (CardLayout)(cardPanel.getLayout());
        cl.show(cardPanel, (String)evt.getItem());
    }
    
    
    @Override
    public void populateFields(List<S> t) {
        cardPanel.removeAll(); //Make sure to remove the old stuff if this is a refresh.
        combo.removeAllItems();
        components = new ArrayList<BuilderJPanel<S>>();
        Integer i = 1;
        for (S s : t) {
            BuilderJPanel<S> p = UIFactory.getUI((Class<? extends UIBuildable>) s.getClass());
            cardPanel.add(p, i.toString()); //Must add to layout before populating or we get a null pointer error.
            p.populateFields(s);
            components.add(p);
            combo.addItem(i.toString());
            i++;
        }
    }
    
    @Override
    public List<S> build() {
        List<S> t;
        try {
            t = this.typeParamClass.newInstance();
        } catch (Exception e) {
            ReportingUtils.showError("Failed to instantiate new: " + this.typeParamClass);
            ReportingUtils.logError(e);
            return null;
        }
        for (BuilderJPanel<S> p : components) {
            S s = p.build();
            t.add(s);
        }
        return t;
    } 
    
    private void addStepAction() {
        List<S> t = this.build();
        S s = t.get(t.size()-1);
        S newS = (S) S.fromJsonString(s.toJsonString(), s.getClass());
        t.add(newS);
        this.populateFields(t);
    }
            
    private void removeStepAction() {
        List<S> t = this.build();
        t.remove(this.combo.getSelectedIndex());
        this.populateFields(t);
    }
    
}