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

package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps;

import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.FileSpecs;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class AcquireCell extends EndpointStep<AcquireCellSettings> {
   private boolean running = false;

   //Represents the acquisition of a single "CellXXX" folder, it can contain multiple PWS, Dynamics, and Fluorescence acquisitions.
   public AcquireCell() {
      super(new AcquireCellSettings(), DefaultSequencerPlugin.Type.ACQ.name());
   }

   @Override
   public boolean isRunning() {
      return running;
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      AcquireCellSettings settings = this.getSettings();
      AcquisitionManager acqMan = Globals.acqManager();
      return new SequencerFunction() {
         @Override
         public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
            running = true;
            status.setAcquisitionlNum(status.getAcquisitionlNum() + 1);
            status.newStatusMessage(
                  String.format("Acquiring Cell %d", status.getAcquisitionlNum()));
            File directory = FileSpecs
                  .getCellFolderName(Paths.get(status.getSavePath()), status.getAcquisitionlNum())
                  .toFile();
            if (!directory.exists()) {
               directory.mkdirs();
            } //The cell folder can be created by the Image saving thread once acquisition begins. In some cases the other thread can get backed up, for safety we just make sure to create the folder right at the beginning.
            if ((!settings.fluorSettings.isEmpty()) && settings.fluorEnabled) {
               status.allowPauseHere();
               acqMan.setFluorescenceSettings(settings.fluorSettings);
               acqMan.acquireFluorescence();
            }
            if (settings.pwsEnabled) {
               status.allowPauseHere();
               acqMan.setPWSSettings(settings.pwsSettings);
               acqMan.acquirePWS();
            }
            if (settings.dynEnabled) {
               status.allowPauseHere();
               acqMan.setDynamicsSettings(settings.dynSettings);
               acqMan.acquireDynamics();
            }
            saveSequenceCoordsFile(status);
            status.allowPauseHere();
            running = false;
            return status;
         }
      };
   }

   private void saveSequenceCoordsFile(AcquisitionStatus status) throws IOException {
      JsonObject obj = status.coords().toJson();
      Path directory = FileSpecs
            .getCellFolderName(Paths.get(status.getSavePath()), status.getAcquisitionlNum());
      String savePath = directory.resolve("sequencerCoords.json").toString();
      try (FileWriter w = new FileWriter(savePath)) {
         GsonUtils.getGson().toJson(obj, w);
      }
   }

   @Override
   protected SimFn getSimulatedFunction() {
      return (Step.SimulatedStatus status) -> {
         status.cellNum++;
         status.requiredPaths
               .add(Paths.get(status.workingDirectory, String.format("Cell%d", status.cellNum))
                     .toString());
         return status;
      };
   }


   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      if (settings.pwsEnabled) {
         String confName = settings.pwsSettings.imConfigName;
         try {
            Globals.getHardwareConfiguration().getImagingConfigurationByName(confName);
         } catch (NoSuchElementException nsee) {
            errs.add(String.format(
                  "PWS Acquisition: No imaging configuration by the name `%s` was found in the hardware configuration.",
                  confName));
         }
      }
      if (settings.dynEnabled) {
         String confName = settings.dynSettings.imConfigName;
         try {
            Globals.getHardwareConfiguration().getImagingConfigurationByName(confName);
         } catch (NoSuchElementException nsee) {
            errs.add(String.format(
                  "Dynamics Acquisition: No imaging configuration by the name `%s` was found in the hardware configuration.",
                  confName));
         }
      }
      if ((!settings.fluorSettings.isEmpty()) && settings.fluorEnabled) {
         for (FluorSettings flSettings : settings.fluorSettings) {
            String confName = flSettings.imConfigName;
            ImagingConfiguration imConf = null;
            try {
               imConf = Globals.getHardwareConfiguration().getImagingConfigurationByName(confName);
            } catch (NoSuchElementException nsee) {
               errs.add(String.format(
                     "Fluorescence Acquisition: No imaging configuration by the name `%s` was found in the hardware configuration.",
                     confName));
            }

            if (flSettings.filterConfigName == null) {
               errs.add("Fluorescence Acquisition: filter configuration name is empty.");
            } else if (imConf != null) {
               List<String> l = Arrays.asList(Globals.core().getAvailableConfigs(imConf.getFluorescenceConfigGroup()).toArray());
               if (!l.contains(flSettings.filterConfigName)) {
                  errs.add(String.format("Fluorescence Acquisition: Filter %s is not a part of the fluorescence configuration group.",
                        flSettings.filterConfigName));
               }
            }
         }
      }
      return errs;
   }
}
