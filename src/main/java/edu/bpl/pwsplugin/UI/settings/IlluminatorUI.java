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

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import mmcorej.DeviceType;
import net.miginfocom.swing.MigLayout;

/**
 * @author nick
 */
public class IlluminatorUI extends BuilderJPanel<IlluminatorSettings> {

   private final JComboBox<String> deviceName = new JComboBox<>();

   public IlluminatorUI() {
      super(new MigLayout(), IlluminatorSettings.class);

      super.add(new JLabel("Device Name:"), "gapleft push");
      super.add(deviceName, "wrap");

      this.updateComboBoxes();
   }

   private void updateComboBoxes() {
      this.deviceName.setModel(new DefaultComboBoxModel<>(
            Globals.core().getLoadedDevicesOfType(DeviceType.ShutterDevice).toArray()));
   }

   @Override
   public void populateFields(IlluminatorSettings settings) {
      deviceName.setSelectedItem(settings.name);
   }

   @Override
   public IlluminatorSettings build() throws BuilderPanelException {
      IlluminatorSettings settings = new IlluminatorSettings();
      settings.name = (String) deviceName.getSelectedItem();
      return settings;
   }
}
