/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ListCardUI<T extends List<S>, S extends JsonableParam> extends ListBuilderJPanel<T> implements ItemListener {
    //A UI component that allows the user to flip through multiple UI componenent representing UIBuildable classes.
    private JComboBox<String> combo = new JComboBox<String>();
    private JPanel cardPanel = new JPanel(new CardLayout());
    JButton addButton = new JButton(" "); //Having a space here keeps the button from collapsing down in size.
    JButton remButton = new JButton(" ");
    JButton moreButton = new JButton("More...");
    JPopupMenu moreMenu = new JPopupMenu("More Menu");
    JMenuItem copyItem = new JMenuItem("Duplicate");
    JMenuItem clearItem = new JMenuItem("Clear All");
    private List<BuilderJPanel<S>> components = new ArrayList<BuilderJPanel<S>>();
    private S[] defaultStepTypes = null;
    private ActionListener action;
     
    
    public ListCardUI(Class<T> clazz,  String msg, S... defaultStepTypes) {
        super(new MigLayout(), clazz);
        
        addButton.setIcon(new ImageIcon(getClass().getResource("/org/micromanager/icons/plus.png")));
        remButton.setIcon(new ImageIcon(getClass().getResource("/org/micromanager/icons/minus.png")));       
        this.addButton.setToolTipText("Add new item");
        this.remButton.setToolTipText("Remove current item");
        addButton.setIconTextGap(0);
        remButton.setIconTextGap(0);
   
        this.defaultStepTypes = defaultStepTypes;
        
        this.cardPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        super.add(new JLabel(msg), "gapleft push");
        super.add(combo);
        super.add(addButton);
        super.add(remButton);
        super.add(moreButton, "wrap");
        super.add(cardPanel, "span, wrap");
        
        copyItem.setEnabled(false);
        remButton.setEnabled(false);
        
        moreMenu.add(copyItem);
        moreMenu.add(clearItem);
        
        combo.setEditable(false);
        combo.addItemListener(this);
        
        addButton.addActionListener(
            (e)->{try{
                this.addStepAction();
            }catch(Exception exc){
                ReportingUtils.showError(exc);
                ReportingUtils.logError(exc);
            }
        });
        
        copyItem.addActionListener(
            (e)->{try{
                this.duplicateStepAction();
            } catch(Exception exc){
                ReportingUtils.showError(exc);
                ReportingUtils.logError(exc);
            }
        });
        
        clearItem.addActionListener((e)->{
            try {
                this.clearAllAction();
            } catch(Exception exc){
                ReportingUtils.showError(exc);
                ReportingUtils.logError(exc);
            }
        });
        
        remButton.addActionListener(
            (e)->{try{
                this.removeStepAction();
            }catch(Exception exc){
                ReportingUtils.showError(exc);
                ReportingUtils.logError(exc);
            }
        });
        
        moreButton.addActionListener((evtttt)->{
            this.moreMenu.show(this.moreButton, 0, 20); //show the menu right on top of the moreButton.
        });
    }
    
    @Override
    public void itemStateChanged(ItemEvent evt) {
        CardLayout cl = (CardLayout)(cardPanel.getLayout());
        cl.show(cardPanel, (String)evt.getItem());
    }
    
    @Override
    public void populateFields(T t) throws Exception {
        cardPanel.removeAll(); //Make sure to remove the old stuff if this is a refresh.
        combo.removeAllItems();
        components = new ArrayList<BuilderJPanel<S>>();
        Integer i = 1;
        for (S s : t) {
            BuilderJPanel<S> p = UIFactory.getUI((Class<?>) s.getClass());
            cardPanel.add(p, i.toString()); //Must add to layout before populating or we get a null pointer error.
            p.populateFields(s);
            components.add(p);
            combo.addItem(i.toString());
            i++;
        }
        if (t.size()>0) {
            this.copyItem.setEnabled(true);
            this.remButton.setEnabled(true);
        } else {
            this.copyItem.setEnabled(false);
            this.remButton.setEnabled(false);
            JLabel l = new JLabel("Empty");
            Font font = new Font("Serif", Font.BOLD, 18);
            l.setFont(font);
            cardPanel.add(l);
        }
    }
    
    @Override
    public T build() {
        T t;
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
    
    private void duplicateStepAction() throws Exception {
        T t = this.build();
        S s;
        if (t.size()>0) {
            s = t.get(this.combo.getSelectedIndex());
        } else {
            return; //this should never happen
        }
        this.actuallyAddStepAction(s);
    }
    
    private void addStepAction() throws Exception {
        if (this.defaultStepTypes.length > 1) { //We have multiple subtypes of S that could be added.
            JPopupMenu menu = new JPopupMenu("Menu");
            for (S step : this.defaultStepTypes) {
                JMenuItem item = new JMenuItem(step.getClass().getSimpleName());
                item.addActionListener((evt)->{
                    try{
                        this.actuallyAddStepAction(step);
                    } catch (Exception e) {
                        ReportingUtils.logError(e);
                    }
                });
                menu.add(item);
            }
            
            menu.show(this.addButton, 0, 0); //show the menu 0,0 from the add button
        } else {
            this.actuallyAddStepAction(this.defaultStepTypes[0]);
        }

    }
    
    private void actuallyAddStepAction(S step) throws Exception {
        T t = this.build();
        S newS = (S) S.fromJsonString(step.toJsonString(), step.getClass()); //Create an independent copy of s.
        t.add(newS);
        this.populateFields(t);
        this.combo.setSelectedIndex(t.size()-1); //Go to the last item. the one we just selected.
        if (this.action != null) {
            this.action.actionPerformed(new ActionEvent(this, 666, "Step Added"));
        }
    }
            
    private void removeStepAction() throws Exception {
        T t = this.build();
        t.remove(this.combo.getSelectedIndex());
        this.populateFields(t);
        if (this.action != null) {
            this.action.actionPerformed(new ActionEvent(this, 666, "Step Removed"));
        }
    }
    
    private void clearAllAction() throws Exception {
        T t = this.build();
        t.clear();
        this.populateFields(t);
        if (this.action != null) {
            this.action.actionPerformed(new ActionEvent(this, 666, "Steps Cleared"));
        }
    }
    
    public void setActionListener(ActionListener act) { //This action listener is fired whenever we add or remove a step.
        action = act;
    }
    
    protected void addItemToMoreButton(JMenuItem item) {
        this.moreMenu.add(item);
    }
}