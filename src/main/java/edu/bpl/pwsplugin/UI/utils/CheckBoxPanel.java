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

import edu.bpl.pwsplugin.UI.utils.disablePanel.DisabledPanel;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
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

      checkBox.addItemListener((evt) -> {
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
