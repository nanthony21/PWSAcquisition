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

import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import java.util.List;
import org.micromanager.data.Image;

/**
 * @author N2-LiveCell
 */
public class SpectralCamera extends DefaultImagingConfiguration {

   Camera _cam;
   TunableFilter _filt;
   Illuminator _illum;

   protected SpectralCamera(ImagingConfigurationSettings settings) throws MMDeviceException {
      super(settings);
      _cam = Camera.getAutomaticInstance(settings.camSettings);
      _filt = TunableFilter.getAutomaticInstance(settings.filtSettings);
      _illum = Illuminator.getAutomaticInstance(settings.illuminatorSettings);
      if ((_illum == null) || (_filt == null) || (_cam == null)) {
         throw new MMDeviceException(String.format(
               "SpectralCamera failed to initialize. Camera: %s. Filter: %s. Illuminator: %s.",
               String.valueOf(_cam), String.valueOf(_filt), String.valueOf(_illum)));
      }
   }

   @Override
   public boolean hasTunableFilter() {
      return true;
   }

   @Override
   public Camera camera() {
      return _cam;
   }

   @Override
   public TunableFilter tunableFilter() {
      return _filt;
   }

   @Override
   public Illuminator illuminator() {
      return _illum;
   }

   @Override
   public List<String> validate() throws MMDeviceException {
      List<String> errs = super.validate();
      errs.addAll(this._cam.validate());
      errs.addAll(this._filt.validate());
      errs.addAll(this._illum.validate());
      errs.addAll(this.zStage().validate());
      return errs;
   }

   public boolean supportsTTLSequencing() {
      return (_cam.supportsTriggerOutput() && _filt.supportsSequencing());
   }

   public void startTTLSequence(int numImages, double delayMs, boolean externalTriggering)
         throws MMDeviceException {
      if (!supportsTTLSequencing()) {
         throw new UnsupportedOperationException(
               "This imaging configuration does not support TTL sequencing.");
      }
      _cam.configureTriggerOutput(true);
      if (externalTriggering) {
         _cam.startSequence(numImages, delayMs, true);
         _filt.startSequence(); //This should trigger a pulse which sets the whole thing off.
      } else { //Since we're not using an external trigger we need to have the camera control the timing.
         _filt.startSequence();
         _cam.startSequence(numImages, delayMs, false);
      }
   }

   public Image snapImage(int wavelength) throws Exception {
      if (_filt.getWavelength() != wavelength) {
         _filt.setWavelength(wavelength);
         while (_filt.isBusy()) {
            Thread.sleep(1);
         } //Wait until the device says it is tuned
      }
      Image im = _cam.snapImage(); //This is so slow.
      return im;
   }

   public void stopTTLSequence() throws Exception {
      _cam.stopSequence();
      _filt.stopSequence();
      _cam.configureTriggerOutput(false);
   }
}
