///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2019
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

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.util.List;

/**
 * A parent acquisition manager that can direct commands down to more specific acquisition managers.
 *    There should only be one of these objects for a given set of hardware in order to avoid trying to run multiple acquisitions at once.
 *    This should be the only way to access any of sublevel acquisition managers.
 */
public class AcquisitionManager {
   private final Acquisition<PWSSettings> pwsManager_ = new PWSAcquisition(new PWSAlbum("PWS"));
   private final Acquisition<DynSettings> dynManager_ = new DynamicsAcquisition(new PWSAlbum("Dynamics"));
   private final Acquisition<List<FluorSettings>> flManager_ = new MultipleFluorescenceAcquisition(
         new PWSAlbum("Fluorescence"));
   private volatile boolean acquisitionRunning_ = false;
   private int cellNum_;
   private String savePath_;

   private synchronized void run(Acquisition<?> manager)
         throws InterruptedException, Exception { //All acquisitions should be run with this.
      if (acquisitionRunning_) {
         throw new IllegalStateException(
               "Attempting to start acquisition when acquisition is already running. How is that possible?");
      }
      acquisitionRunning_ = true;
      try {
         if (Globals.mm().live().isLiveModeOn()) {
            Globals.mm().live().setLiveModeOn(false);
         }
         manager.acquireImages(savePath_, cellNum_);
      } catch (InterruptedException ie) {
         Thread.currentThread().interrupt();
         throw ie;
      } finally {
         acquisitionRunning_ = false;
      }
   }

   public void setFluorescenceSettings(List<FluorSettings> settings) {
      flManager_.setSettings(settings);
   }

   public void setCellNum(int num) {
      cellNum_ = num;
   }

   public void setSavePath(String savePath) {
      savePath_ = savePath;
   }

   public String getSavePath() {
      return savePath_;
   }

   public void setPWSSettings(PWSSettings settings) throws Exception {
      pwsManager_.setSettings(settings);
   }

   public void setDynamicsSettings(DynSettings settings) {
      dynManager_.setSettings(settings);
   }

   public void acquirePWS() throws InterruptedException, Exception {
      run(pwsManager_);
   }

   public void acquireDynamics() throws InterruptedException, Exception {
      run(dynManager_);
   }

   public void acquireFluorescence() throws InterruptedException, Exception {
      run(flManager_);
   }

   public boolean isAcquisitionRunning() {
      return acquisitionRunning_;
   }
}
