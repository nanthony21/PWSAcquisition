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

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import java.util.ArrayList;
import java.util.List;

/**
 * @author N2-LiveCell
 */
public abstract class DefaultImagingConfiguration implements ImagingConfiguration {

   protected ImagingConfigurationSettings settings;
   private boolean initialized_ = false;
   protected TranslationStage1d zStage;
   private boolean activated_ = false;

   protected DefaultImagingConfiguration(ImagingConfigurationSettings settings) {
      this.settings = settings;
   }

   @Override
   public TranslationStage1d zStage() {
      return zStage;
   }

   private void initialize() throws MMDeviceException { //One-time initialization of devices
      zStage = TranslationStage1d.getAutomaticInstance();
      if (zStage == null) {
         throw new MMDeviceException("No supported Z-stage was found.");
      }
      camera().initialize();
      if (hasTunableFilter()) {
         tunableFilter().initialize();
      }
      illuminator().initialize();
      initialized_ = true;
   }

   //We only want the following functions to be accessed by the HWConfigrartion
   @Override
   public void activateConfiguration()
         throws MMDeviceException { //Actually configure the hardware to use this configuration.
      if (!initialized_) {
         this.initialize(); //If we haven't yet then run the one-time initialization for the the devices.
      }
      try {
         boolean liveMode = false;
         if (Globals.mm().live().isLiveModeOn()) {
            liveMode = true;
            //We need to turn off live mode for this step or we can get errors.
            Globals.mm().live().setLiveModeOn(false);
         }
         Globals.core().setConfig(settings.configurationGroup,
               settings.configurationName); //Get this process started, it can sometimes take some time.
         camera().activate();
         if (hasTunableFilter()) {
            tunableFilter().activate();
         }
         illuminator().activate();
         Globals.core().waitForConfig(settings.configurationGroup,
               settings.configurationName); //Make sure to let the config group change finish before proceeding.
         if (liveMode) {
            Globals.mm().live().setLiveModeOn(true); //Reenable live mode if it was on.
         }
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
      activated_ = true;
   }

   @Override
   public void deactivateConfiguration() {
      activated_ = false;
   }

   @Override
   public String getFluorescenceConfigGroup() {
      if (settings.fluorescenceConfigGroup
            .equals(ImagingConfigurationSettings.MANUALFLUORESCENCENAME)) {
         return null;
      } else {
         return settings.fluorescenceConfigGroup;
      }
   }

   @Override
   public List<String> validate() throws MMDeviceException {
      if (!initialized_) {
         this.initialize(); //If we don't do this then many of the device variables will not yet be initialized.
      }
      List<String> errs = new ArrayList<>();
      //TODO check for null pointers (zstage, etc.)
      if (settings.name.equals("")) {
         errs.add("Imaging configuration must have a name.");
      }
      errs.addAll(zStage.validate());
      return errs;
   }
    
    /*boolean isActive() throws MMDeviceException {
        if (!activated_) { return false; }
        try { //Even if the `activated_` flag is true we still check that the configuration group is properly set, just to make sure.
            boolean active = Globals.core().getCurrentConfig(settings.configurationGroup).equals(settings.configurationName);
            return active;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MMDeviceException(ie);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }*/


}

