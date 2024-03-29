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

package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.HardwareManager;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import java.util.List;

/**
 * @author N2-LiveCell
 */
public class StandardCamera extends DefaultImagingConfiguration {

   Camera _cam;
   Illuminator _illum;

   protected StandardCamera(ImagingConfigurationSettings settings) throws MMDeviceException {
      super(settings);
      _cam = (Camera) HardwareManager.instance().getDevice(settings.camSettings);
      _illum = (Illuminator) HardwareManager.instance().getDevice(settings.illuminatorSettings);
      if ((_illum == null) || (_cam == null)) {
         throw new MMDeviceException("StandardCamera failed to initialize");
      }
   }

   @Override
   public boolean hasTunableFilter() {
      return false;
   }

   @Override
   public Camera camera() {
      return _cam;
   }

   @Override
   public TunableFilter tunableFilter() {
      throw new UnsupportedOperationException("StandardCamera configuration has no tunable filter");
   }

   @Override
   public Illuminator illuminator() {
      return _illum;
   }

   @Override
   public List<String> validate() throws MMDeviceException {
      List<String> errs = super.validate();
      errs.addAll(this._cam.validate());
      errs.addAll(this._illum.validate());
      errs.addAll(this.zStage().validate());
      return errs;
   }
}
