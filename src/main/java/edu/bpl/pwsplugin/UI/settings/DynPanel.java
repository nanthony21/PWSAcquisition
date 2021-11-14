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
import edu.bpl.pwsplugin.settings.DynSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class DynPanel extends SingleBuilderJPanel<DynSettings> {

   private final ImprovedComponents.Spinner wvSpinner = new ImprovedComponents.Spinner();
   private final ImprovedComponents.Spinner framesSpinner = new ImprovedComponents.Spinner();
   private final ImprovedComponents.Spinner exposureSpinner = new ImprovedComponents.Spinner();
   private final JComboBox<String> imConfName = new JComboBox<>();


   public DynPanel() {
      super(new MigLayout(), DynSettings.class);
      wvSpinner.setModel(new SpinnerNumberModel(550, 400, 1000, 5));
      framesSpinner.setModel(new SpinnerNumberModel(200, 1, 1000, 1));
      exposureSpinner.setModel(new SpinnerNumberModel(50, 1, 500, 5));

      super.add(new JLabel("Wavelength (nm)"), "gapleft push");
      super.add(wvSpinner, "wrap");
      super.add(new JLabel("Exposure (ms)"), "gapleft push");
      super.add(exposureSpinner, "wrap, growx");
      super.add(new JLabel("# of Frames"), "gapleft push");
      super.add(framesSpinner, "wrap");
      super.add(new JLabel("Imaging Configuration:"), "span");
      super.add(imConfName, "span");
   }

   @Override
   public Map<String, Object> getPropertyFieldMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("exposure", exposureSpinner);
      map.put("wavelength", wvSpinner);
      map.put("numFrames", framesSpinner);
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
