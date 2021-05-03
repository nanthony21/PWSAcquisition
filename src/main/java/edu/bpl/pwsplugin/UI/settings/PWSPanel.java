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

package edu.bpl.pwsplugin.UI.settings;

import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 * @author nick
 */
public class PWSPanel extends SingleBuilderJPanel<PWSSettings> {

   private final ImprovedComponents.Spinner exposureSpinner;
   private final ImprovedComponents.Spinner wvStartSpinner;
   private final ImprovedComponents.Spinner wvStopSpinner;
   private final ImprovedComponents.Spinner wvStepSpinner;
   private final JComboBox<String> imConfName = new JComboBox<>();
   private final JCheckBox ttlTriggerCheckbox = new JCheckBox("Use TTL Sequencing");
   private final JCheckBox externalTriggerCheckBox = new JCheckBox("Use External TTL Trigger");

   public PWSPanel() {
      super(new MigLayout(), PWSSettings.class);

      exposureSpinner = new ImprovedComponents.Spinner(new SpinnerNumberModel(100, 1, 1000, 5));
      wvStartSpinner = new ImprovedComponents.Spinner(new SpinnerNumberModel(500, 400, 1000, 5));
      wvStartSpinner.setToolTipText("In nanometers. The wavelength to start scanning at.");

      wvStopSpinner = new ImprovedComponents.Spinner(new SpinnerNumberModel(700, 400, 1000, 5));
      wvStopSpinner.setToolTipText("In nanometers. The wavelength to stop scanning at.");

      wvStepSpinner = new ImprovedComponents.Spinner(new SpinnerNumberModel(2, 1, 50, 1));

      ttlTriggerCheckbox.setToolTipText(
            "Whether the camera should be configured to trigger wavelength changes in the filter over TTL. This may not be supported.");
      ttlTriggerCheckbox.addItemListener((evt) -> {
         boolean checked = ttlTriggerCheckbox.isSelected();
         if (!checked) {
            externalTriggerCheckBox.setSelected(false);
         }
         externalTriggerCheckBox.setEnabled(checked);
      });

      externalTriggerCheckBox.setToolTipText(
            "Whether the filter should trigger a new camera acquisition over TTL. This is not possible for LCTF but can be done with the VF-5 Filter.");
      externalTriggerCheckBox.setEnabled(false);

      super.add(new JLabel("Start (nm)"));
      super.add(new JLabel("Stop (nm)"));
      super.add(new JLabel("Step (nm)"));
      super.add(new JLabel("Exposure (ms)"), "wrap");
      super.add(wvStartSpinner);
      super.add(wvStopSpinner);
      super.add(wvStepSpinner);
      super.add(exposureSpinner, "wrap");
      super.add(ttlTriggerCheckbox, "wrap, span");
      super.add(externalTriggerCheckBox, "wrap, span");
      super.add(new JLabel("Imaging Configuration:"), "span");
      super.add(imConfName, "span");
   }


   @Override
   public Map<String, Object> getPropertyFieldMap() {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("wvStart", wvStartSpinner);
      map.put("wvStop", wvStopSpinner);
      map.put("wvStep", wvStepSpinner);
      map.put("exposure", exposureSpinner);
      map.put("ttlTriggering", ttlTriggerCheckbox);
      map.put("externalCamTriggering", externalTriggerCheckBox);
      map.put("imConfigName", imConfName);
      return map;
   }

   //API
   public void setExposure(double exposureMs) {
      this.exposureSpinner.setValue(exposureMs);
   }

   public void setAvailableConfigNames(List<String> names) {
      this.imConfName.removeAllItems();
      if (names.isEmpty()) {
         this.imConfName.addItem("NONE!"); //Prevent a null pointer error.
      } else {
         for (String name : names) {
            this.imConfName.addItem(name);
         }
      }
   }
}
