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
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageIOSaver;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageSaver;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DoubleVector;
import org.micromanager.data.Image;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * A base class for an acquisition that acquires from a list of settings and puts the resulting
 * images all into a shared display. Images are saved to individual numbered folders.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
abstract class ListAcquisitionBase<S> implements Acquisition<List<S>> {
   protected List<S> settingsList;
   private final PWSAlbum display;

   protected ListAcquisitionBase(PWSAlbum album) {
      this.display = album;
   }


   @Override
   public void acquireImages(String savePath, int cellNum) throws Exception {
      this.display
            .clear(); //The implementation of `runSingleImageAcquisition` call `displayImage` to add images to the display throughout the imaging process.
      initializeAcquisitions();
      try {
         for (int i = 0; i < this.settingsList.size(); i++) {
            S settings = this.settingsList.get(i);
            this.setCurrentSettings(settings);
            ImagingConfiguration imConf = this
                  .getImgConfig(); //Activation must occur every time the imaging configuration changes. Initializemetadata requires that the correct configuration is active.
            if (!(imConf == Globals.getHardwareConfiguration()
                  .getActiveConfiguration())) { //It's important that the configuration is activated before we try pulling metadata like the affine transform
               Globals.getHardwareConfiguration().activateImagingConfiguration(
                     imConf); //Activation must occur every time the imaging configuration changes.
            }
            MetadataBase md = this.initializeMetadata(imConf);
            String subFolderName = String
                  .format("%s_%d", FileSpecs.getSubfolderName(this.getFileType()), i);
            Path fullSavePath = FileSpecs.getCellFolderName(Paths.get(savePath), cellNum)
                  .resolve(subFolderName);
            ImageSaver imSaver = new ImageIOSaver();
            imSaver.configure(fullSavePath.toString(), FileSpecs.getFilePrefix(this.getFileType()),
                  this.numFrames());
            this.runSingleImageAcquisition(imSaver, md);
         }
      } finally {
         this.finalizeAcquisitions();
      }
   }

   /**
    * Set the list of settings to iterate over.
    * @param settingList
    */
   @Override
   public final void setSettings(List<S> settingList) {
      this.settingsList = settingList;
   }

   private MetadataBase initializeMetadata(ImagingConfiguration imConf) throws Exception {
      if (Globals.core().getPixelSizeUm()
            == 0.0) { //This information gets saved to the metadata below in the form of an affine transform.
         ReportingUtils.showMessage(
               "It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
      }
      DoubleVector aff = Globals.core().getPixelSizeAffine();
      List<Double> trans = new ArrayList<>();
      for (int i = 0; i < aff.size(); i++) {
         trans.add(aff.get(i));
      }

      MetadataBase metadata = new MetadataBase(
            imConf.camera().getSettings().linearityPolynomial,
            Globals.getHardwareConfiguration().getSettings().systemName,
            imConf.camera().getSettings().darkCounts,
            trans);
      return metadata;
   }

   /**
    * Configure the acquisition with these settings.
    * @param settings
    */
   protected abstract void setCurrentSettings(S settings);

   /**
    *
    * @return The ImagingConfiguration we are currnetly configured to use.
    */
   protected abstract ImagingConfiguration getImgConfig();

   /**
    * Clean up the acquisitions.
    * @throws Exception
    */
   protected abstract void finalizeAcquisitions() throws Exception;

   /**
    * Setup before starting the list of acquisitions.
    * @throws Exception
    */
   protected abstract void initializeAcquisitions() throws Exception;

   /**
    * Run a single acquisitions
    * @param saver
    * @param md
    * @throws Exception
    */
   protected abstract void runSingleImageAcquisition(ImageSaver saver, MetadataBase md)
         throws Exception;

   /**
    * @return The type enumerator for this acquisition, used for file saving information.
    */
   protected abstract FileSpecs.Type getFileType();

   /**
    *
    * @return The number of image planes that will be acquired in a single Acquisition (e.g. Fluorescence=1, PWS=101)
    */
   protected abstract Integer numFrames();

   /**
    * Add an image to the display
    * @param img
    */
   protected void displayImage(Image img) { //Call this from within the implementation to add images to the display.
      this.display.addImage(img);
   }
}
