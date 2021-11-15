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
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageIOSaver;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageSaver;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DoubleVector;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * A base class for an acquisition manager that handles instantiation of the required metadata for every type of image.
 * This class assumes that the same imaging configuration will be used throughout the whole imaging process, which may not be true.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
abstract class SingleAcquisitionBase<S> implements Acquisition<S> {

   /**
    *
    * @param savePath The path to save to
    * @param cellNum  The number of acquisition (determines folder naming).
    * @throws Exception
    */
   @Override
   public final void acquireImages(String savePath, int cellNum) throws Exception {
      MetadataBase metadata = this.initializeMetadata();
      ImageSaver imSaver = new ImageIOSaver();
      imSaver.configure(this.getSavePath(savePath, cellNum),
            FileSpecs.getFilePrefix(this.getFileType()), this.numFrames());
      this._acquireImages(imSaver, metadata);
   }

   /**
    *
    * @return
    * @throws Exception
    */
   private MetadataBase initializeMetadata() throws Exception {
      ImagingConfiguration imConf = this.getImgConfig();
      if (!(imConf == Globals.getHardwareConfiguration()
            .getActiveConfiguration())) { //It's important that the configuration is activated before we try pulling metadata like the affine transform
         Globals.getHardwareConfiguration().activateImagingConfiguration(
               imConf); //Activation must occur every time the imaging configuration changes.
      }
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

      return new MetadataBase(
            imConf.camera().getSettings().linearityPolynomial,
            Globals.getHardwareConfiguration().getSettings().systemName,
            imConf.camera().getSettings().darkCounts,
            trans);
   }

   /**
    * Get the ImagingConfiguration that this acquisition will use.
    * @return
    */
   protected abstract ImagingConfiguration getImgConfig();

   /**
    * Given a save directory and an acquisition number return the full path that will be saved to.
    * @param savePath
    * @param cellNum
    * @return
    * @throws FileAlreadyExistsException
    */
   protected abstract String getSavePath(String savePath, int cellNum)
         throws FileAlreadyExistsException;

   /**
    *
    * @return The type enumerator for this acquisition, used for file saving information.
    */
   protected abstract FileSpecs.Type getFileType();

   /**
    *
    * @return The number of images that will be acquired.
    */
   protected abstract Integer numFrames();

   /**
    * Subclasses should implement the actual image acquisition routine here.
    * @param saver The object that takes images into a queue for saving.
    * @param md The basic metadata which will need to be populated and passed to the saver.
    * @throws Exception
    */
   protected abstract void _acquireImages(ImageSaver saver, MetadataBase md) throws Exception;

}
