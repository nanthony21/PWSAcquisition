///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
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

package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.CardLayout;
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
 * @param <T> The class of the object that hold settings. Must be an list of `jsonableparam`s.
 * @param <S> The subclass of `JsonableParam` that holds settings for each item of the list.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class ListCardUI<T extends List<S>, S extends JsonableParam> extends
      ListBuilderJPanel<T> implements ItemListener {

   //A UI component that allows the user to flip through multiple UI componenent representing UIBuildable classes.
   private JComboBox<String> combo = new JComboBox<String>();
   private JPanel cardPanel = new JPanel(new CardLayout());
   JButton addButton = new JButton(
         " "); //Having a space here keeps the button from collapsing down in size.
   JButton removeButton = new JButton(" ");
   JButton moreButton = new JButton("More...");
   JPopupMenu moreMenu = new JPopupMenu("More Menu");
   JMenuItem copyItem = new JMenuItem("Duplicate");
   JMenuItem clearItem = new JMenuItem("Clear All");
   private List<BuilderJPanel<S>> components = new ArrayList<>();
   private S[] defaultStepTypes = null;
   private ActionListener action;


   public ListCardUI(Class<T> clazz, String msg, S... defaultStepTypes) {
      super(new MigLayout("insets 0"), clazz);

      addButton.setIcon(new ImageIcon(getClass().getResource("/org/micromanager/icons/plus.png")));
      removeButton
            .setIcon(new ImageIcon(getClass().getResource("/org/micromanager/icons/minus.png")));
      this.addButton.setToolTipText("Add new item");
      this.removeButton.setToolTipText("Remove current item");
      addButton.setIconTextGap(0);
      removeButton.setIconTextGap(0);

      this.defaultStepTypes = defaultStepTypes;

      this.cardPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      super.add(new JLabel(msg), "gapleft push");
      super.add(combo);
      super.add(addButton);
      super.add(removeButton);
      super.add(moreButton, "wrap");
      super.add(cardPanel, "span, wrap");

      try {
         this.actuallyAddStepAction(
               this.defaultStepTypes[0]); // Add the default step so we at least have something there. Helps to get the initial component sized right.
      } catch (BuilderPanelException e) {
         ReportingUtils.logError(e);
      }

      copyItem.setEnabled(false);
      removeButton.setEnabled(false);

      moreMenu.add(copyItem);
      moreMenu.add(clearItem);

      combo.setEditable(false);
      combo.addItemListener(this);

      addButton.addActionListener(
            (e) -> {
               try {
                  this.addStepAction();
               } catch (BuilderPanelException exc) {
                  ReportingUtils.showError(exc);
                  ReportingUtils.logError(exc);
               }
            });

      copyItem.addActionListener(
            (e) -> {
               try {
                  this.duplicateStepAction();
               } catch (BuilderPanelException exc) {
                  ReportingUtils.showError(exc);
                  ReportingUtils.logError(exc);
               }
            });

      clearItem.addActionListener((e) -> {
         try {
            this.clearAllAction();
         } catch (BuilderPanelException exc) {
            ReportingUtils.showError(exc);
            ReportingUtils.logError(exc);
         }
      });

      removeButton.addActionListener(
            (e) -> {
               try {
                  this.removeStepAction();
               } catch (BuilderPanelException exc) {
                  ReportingUtils.showError(exc);
                  ReportingUtils.logError(exc);
               }
            });

      moreButton.addActionListener((evtttt) -> {
         this.moreMenu.show(this.moreButton, 0, 20); //show the menu right on top of the moreButton.
      });
   }

   public List<BuilderJPanel<S>> getSubComponents() {
      return this.components;
   }

   @Override
   public void itemStateChanged(ItemEvent evt) {
      CardLayout cl = (CardLayout) (cardPanel.getLayout());
      cl.show(cardPanel, (String) evt.getItem());
   }

   @Override
   public void populateFields(T t) throws BuilderPanelException {
      cardPanel.removeAll(); //Make sure to remove the old stuff if this is a refresh.
      combo.removeAllItems();
      components = new ArrayList<BuilderJPanel<S>>();
      Integer i = 1;
      for (S s : t) {
         BuilderJPanel<S> p = UIFactory.getUI((Class<? extends JsonableParam>) s.getClass());
         cardPanel.add(p,
               i.toString()); //Must add to layout before populating or we get a null pointer error.
         p.populateFields(s);
         components.add(p);
         combo.addItem(i.toString());
         i++;
      }
      if (t.size() > 0) {
         this.copyItem.setEnabled(true);
         this.removeButton.setEnabled(true);
      } else {
         this.copyItem.setEnabled(false);
         this.removeButton.setEnabled(false);
         JLabel l = new JLabel("Empty");
         Font font = new Font("Serif", Font.BOLD, 18);
         l.setFont(font);
         cardPanel.add(l);
      }
   }

   @Override
   public T build() throws BuilderPanelException {
      T t;
      try {
         t = this.typeParamClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
         throw new BuilderPanelException(e);
      }
      for (BuilderJPanel<S> p : components) {
         S s = p.build();
         t.add(s);
      }
      return t;
   }

   private void duplicateStepAction() throws BuilderPanelException {
      T t = this.build();
      S s;
      if (t.size() > 0) {
         s = t.get(this.combo.getSelectedIndex());
      } else {
         throw new BuilderPanelException("this should never happen");
      }
      this.actuallyAddStepAction(s);
   }

   private void addStepAction() throws BuilderPanelException {
      if (this.defaultStepTypes.length > 1) { //We have multiple subtypes of S that could be added.
         JPopupMenu menu = new JPopupMenu("Menu");
         for (S step : this.defaultStepTypes) {
            JMenuItem item = new JMenuItem(step.getClass().getSimpleName());
            item.addActionListener((evt) -> {
               try {
                  this.actuallyAddStepAction(step);
               } catch (BuilderPanelException e) {
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

   private void actuallyAddStepAction(S step) throws BuilderPanelException {
      T t = this.build();
      S newS = (S) S
            .fromJson(step.toJsonString(), step.getClass()); //Create an independent copy of s.
      t.add(newS);
      this.populateFields(t);
      this.combo.setSelectedIndex(t.size() - 1); //Go to the last item. the one we just selected.
      if (this.action != null) {
         this.action.actionPerformed(new ActionEvent(this, 666, "Step Added"));
      }
   }

   private void removeStepAction() throws BuilderPanelException {
      T t = this.build();
      t.remove(this.combo.getSelectedIndex());
      this.populateFields(t);
      if (this.action != null) {
         this.action.actionPerformed(new ActionEvent(this, 666, "Step Removed"));
      }
   }

   private void clearAllAction() throws BuilderPanelException {
      T t = this.build();
      t.clear();
      this.populateFields(t);
      if (this.action != null) {
         this.action.actionPerformed(new ActionEvent(this, 666, "Steps Cleared"));
      }
   }

   public void setActionListener(
         ActionListener act) { //This action listener is fired whenever we add or remove a step.
      action = act;
   }

   protected void addItemToMoreButton(JMenuItem item) {
      this.moreMenu.add(item);
   }
}