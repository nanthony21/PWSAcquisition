/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.UI.utils.disablePanel.DisabledPanel;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */

public class CheckBoxPanel extends JPanel {

   JCheckBox checkBox;
   JPanel contentsPanel;
   Map<Component, Object> _enabledState;

    public CheckBoxPanel(JPanel contents, String title) {
        super(new MigLayout("insets 0 0 0 0"));
        checkBox = new JCheckBox(title);
        checkBox.setFont(new Font("serif", Font.BOLD, 11) {
        });
        contentsPanel = new DisabledPanel(contents);

        super.add(checkBox, "wrap 0"); // 0 gap between the components.
        super.add(contentsPanel);

        checkBox.addItemListener((evt)-> {
              boolean enable = checkBox.isSelected();
              contentsPanel.setEnabled(enable);
           });
        checkBox.setSelected(true);
    }
   
   public boolean isSelected() {
      return checkBox.isSelected();
   }

   public void setSelected(boolean selected) {
      checkBox.setSelected(selected);
      contentsPanel.setEnabled(selected);
   }

   public void addActionListener(ActionListener actionListener) {
      checkBox.addActionListener(actionListener);
   }

   public void removeActionListeners() {
      for (ActionListener l : checkBox.getActionListeners()) {
         checkBox.removeActionListener(l);
      }
   }
}
