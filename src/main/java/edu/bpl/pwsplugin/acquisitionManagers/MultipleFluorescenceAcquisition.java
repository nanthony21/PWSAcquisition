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

package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.FileSpecs;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageSaver;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import edu.bpl.pwsplugin.metadata.FluorescenceMetadata;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * Acquires multiple fluorescence images from a list of fluorescence settings.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
class MultipleFluorescenceAcquisition extends ListAcquisitionBase<FluorSettings> {
   //Some weird stuff can happen with the display when different iterations have different resolutions. that's ok though.

   //This map contains all initial configuration states for the configuration groups to adjust fluorescence filter. This is populated during initialization and then used during finalization.
   private Map<String, String> initialFilters;
   private AcquisitionContext context_; // The object that tracks information specific to a single acquisition in the list

   public MultipleFluorescenceAcquisition(PWSAlbum display) {
      super(display);
   }

   @Override
   protected void initializeAcquisitions(List<FluorSettings> settingsList) throws MMDeviceException {

      //Imaging configuration isn't set at this point. A single set of acquisitions may contain multiple imaging configurations so we need to consider initialization for each one.
      initialFilters = new HashMap<>();
      for (FluorSettings settings : settingsList) {
         ImagingConfiguration imagingConf = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.imConfigName);
         String confGroup = imagingConf.getFluorescenceConfigGroup();
         if (confGroup != null) {
            String filt;
            try {
               filt = Globals.core().getCurrentConfig(confGroup);
            } catch (Exception e) {
               throw new MMDeviceException(e);
            }
            initialFilters.put(confGroup, filt);
         } else {
            initialFilters.put(ImagingConfigurationSettings.MANUALFLUORESCENCENAME, null);
         }
      }

      boolean hasOffset = false;
      for (FluorSettings settings : super.settingsList) {
         if (settings.focusOffset != 0) {
            hasOffset = true;
            break;
         }
      }

      TranslationStage1d zStage = Globals.getHardwareConfiguration().getActiveConfiguration().zStage();
      Double originalZPos = null;
      if (hasOffset) {
         originalZPos = zStage.getPosUm();
      }

      final boolean needToReEnableFocusLock;
      if (zStage.getAutoFocusEnabled() && hasOffset) {
         needToReEnableFocusLock = true;
         zStage.setAutoFocusEnabled(false);  // We don't want to use focus lock for moving around to fluorescence offsets, it's too slow. We will disable and then re-enable at the end.
      } else {
         needToReEnableFocusLock = false;
      }
      context_ = new AcquisitionContext(zStage, originalZPos, needToReEnableFocusLock);
   }

   @Override
   protected void setCurrentSettings(FluorSettings settings) {
      context_.setCurrentSettings(settings);
   }

   @Override
   protected void runSingleImageAcquisition(ImageSaver imSaver, MetadataBase metadata) throws Exception {
      Globals.logger().logDebug(String.format("Multiple Fluorescence Acquisition beginning. %s", context_.currentSettings.toJsonString()));
      boolean spectralMode = context_.currentImagingConfig.hasTunableFilter();
      String fluorConfigGroup = context_.currentImagingConfig.getFluorescenceConfigGroup();
      if (fluorConfigGroup != null) {
         Globals.core().setConfig(fluorConfigGroup, context_.currentSettings.filterConfigName);
         Globals.core().waitForConfig(fluorConfigGroup, context_.currentSettings.filterConfigName); // Wait for the device to be ready.
      } else {
         ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
      }
      if (spectralMode) {
         context_.currentImagingConfig.tunableFilter().setWavelength(context_.currentSettings.tfWavelength);
      }
      if (context_.currentSettings.focusOffset != 0) {
         context_.zStage.setPosUm(context_.originalZPos + context_.currentSettings.focusOffset);
      }
      try {
         imSaver.beginSavingThread();
         context_.currentImagingConfig.camera().setExposure(context_.currentSettings.exposure);
         Globals.core().clearCircularBuffer();
         Image img = context_.currentImagingConfig.camera().snapImage();
         metadata.setMicroManagerMetadata(img);
         Integer wv;
         if (spectralMode) {
            wv = context_.currentSettings.tfWavelength;
         } else {
            wv = null;
         }
         //This must happen after we have set our exposure.
         FluorescenceMetadata flmd = new FluorescenceMetadata(metadata, context_.currentSettings.filterConfigName, context_.currentImagingConfig.camera().getExposure(), wv);
         //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
         Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true);
         Coords coords = img.getCoords();
         pipeline.insertImage(img); //Add image to the data pipeline for processing
         img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.
         imSaver.setMetadata(flmd);
         this.displayImage(img);
         imSaver.addImage(img);
      } finally {
         Globals.logger().logDebug("Multiple Fluorescence Acquisition ending.");
      }
   }

   @Override
   protected void finalizeAcquisitions() throws MMDeviceException {
      //Re-set all fluorescence filters to initial state.
      for (Map.Entry<String, String> entry : initialFilters.entrySet()) {
         String fluorConfigGroup = entry.getKey();
         String configState = entry.getValue();
         if (fluorConfigGroup.equals(
               ImagingConfigurationSettings.MANUALFLUORESCENCENAME)) { //Manual filter control
            ReportingUtils.showMessage("Return to the initial filter block and click `OK`.",
                  Globals.frame());
         } else { //Automatic filter control
            try {
               Globals.core().setConfig(fluorConfigGroup, configState);
               Globals.core().waitForConfig(fluorConfigGroup,
                     configState); // Wait for the device to be ready.
            } catch (Exception e) {
               throw new MMDeviceException(e);
            }
         }
      }

      if (context_.originalZPos != null) {
         try {
            context_.zStage.setPosUm(context_.originalZPos);
         } catch (InterruptedException e) {
            throw new MMDeviceException(e);
         }
      }
      if (context_.needToReEnableFocusLock) {
         context_.zStage.setAutoFocusEnabled(true);
      }
      context_ = null; // Make sure we get a null pointer error instead of weird leftover data if there is a programming error.
   }

   @Override
   protected Integer numFrames() {
      return 1;
   }

   @Override
   protected ImagingConfiguration getImgConfig() {
      return context_.currentImagingConfig;
   }

   @Override
   protected FileSpecs.Type getFileType() {
      return FileSpecs.Type.FLUORESCENCE;
   }

   /**
    * The acquisition manager is a single instance that handles multiple listacquisitions. Keep track of info that is only relevant to a single listacquisition in
    * this class.
    */
   static class AcquisitionContext {
      public FluorSettings currentSettings;
      public ImagingConfiguration currentImagingConfig;
      public final boolean needToReEnableFocusLock;  // Keeps track of if we disabled focus lock during the initialization.
      public final Double originalZPos; // If using offsets we will record this position and return to it at the end. Leave as `null` to ignore this.
      public final TranslationStage1d zStage; //TODO we have a weird situation where the `context` may have multiple imaging configs (each with a new instance of a zStage) but we then have this instance that is constant over a single list acquisition.

      public AcquisitionContext(TranslationStage1d zStage, Double originalZPos, boolean needToReEnableFocusLock) {
         this.zStage = zStage;
         this.originalZPos = originalZPos;
         this.needToReEnableFocusLock = needToReEnableFocusLock;
      }

      public void setCurrentSettings(FluorSettings settings) {
         currentSettings = settings;
         currentImagingConfig = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.imConfigName);
      }
   }

}
