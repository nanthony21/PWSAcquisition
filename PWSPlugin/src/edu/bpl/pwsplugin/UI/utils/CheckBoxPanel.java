/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */

public class CheckBoxPanel extends JPanel {

   JCheckBox checkBox;
   JPanel contentsPanel;
   Map<Component, Object> _enabledState;

    public CheckBoxPanel(LayoutManager layout, String title) {
        super(new MigLayout("insets 0 0 0 0"));
        checkBox = new JCheckBox(title); //Make this bold
        checkBox.setFont(new Font("serif", Font.BOLD, 11) {
        });
        contentsPanel = new JPanel(layout);

        super.add(checkBox, "wrap, gapy 1px");
        super.add(contentsPanel);

        checkBox.addItemListener((evt)-> {
              boolean enable = checkBox.isSelected();
              setChildrenEnabled(enable);
           });
        scanEnableState();
        checkBox.setSelected(true);
    }
   
    public void setContentBorder(Border border) {
        contentsPanel.setBorder(border);
    }
   
    @Override
    public void add(Component comp, Object constraints) {
        contentsPanel.add(comp, constraints);
    }
   
    @Override
    public Component add(Component comp) {
        contentsPanel.add(comp);
        return comp;
    }
    @Override
    public Component add(Component comp, int index) {
        contentsPanel.add(comp, index);
        return comp;
    }
    @Override
    public void add(Component comp, Object constraints, int index) {
        contentsPanel.add(comp, constraints, index);
    }
    @Override
    public Component add(String name, Component comp) {
        contentsPanel.add(name, comp);
        return comp;
    }
   
   private void scanEnableState() {
       _enabledState = _scanEnabledState(this.contentsPanel);
   }
   
   private Map<Component, Object> _scanEnabledState(Container panel) {
        Map<Component, Object> m = new HashMap<>();
        Component comp[] = panel.getComponents();
        for (Component comp1 : comp) {
            if (((Container) comp1).getComponentCount() > 0) {
                m.put(comp1, _scanEnabledState((Container) comp1));
            } else {
                m.put(comp1, comp1.isEnabled());
            }
        }
        return m;
   }

   public void setChildrenEnabled(boolean enabled) {
       if (!enabled) {
           scanEnableState();
       }
       _setChildrenEnabled(this.contentsPanel, enabled, _enabledState);
   }
   
   private void _setChildrenEnabled(Container panel, boolean enabled, Map<Component, Object> map) {
        Component comp[] = panel.getComponents();
          for (Component comp1 : comp) {
             if (((Container)comp1).getComponentCount()>0) {
                 _setChildrenEnabled((Container) comp1, enabled, (Map<Component, Object>) map.get(comp1));   
             } else {
                boolean wasEnabled = (boolean) map.get(comp1);
                if (wasEnabled) {
                   comp1.setEnabled(enabled);
                }
             }
          }
   }
   
   public boolean isSelected() {
      return checkBox.isSelected();
   }

   public void setSelected(boolean selected) {
      checkBox.setSelected(selected);
      setChildrenEnabled(selected);
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
