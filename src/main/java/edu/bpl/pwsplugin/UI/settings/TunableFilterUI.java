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
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import mmcorej.DeviceType;
import net.miginfocom.swing.MigLayout;

/**
 * @author N2-LiveCell
 */
public class TunableFilterUI extends SingleBuilderJPanel<TunableFilterSettings> {

   private final JComboBox<String> name = new JComboBox<>();

   public TunableFilterUI() {
      super(new MigLayout(), TunableFilterSettings.class);

      this.add(new JLabel("Device Name:"), "gapleft push");
      this.add(name, "wrap");

      this.name.setModel(new DefaultComboBoxModel<>(
            Globals.core().getLoadedDevicesOfType(DeviceType.AnyType).toArray()));
   }

   @Override
   public Map<String, Object> getPropertyFieldMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("name", name);
      return map;
   }
}
