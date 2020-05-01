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
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionManagers.PWSAcqManager;
import edu.bpl.pwsplugin.acquisitionManagers.DynAcqManager;
import edu.bpl.pwsplugin.acquisitionManagers.fluorescence.AltCamFluorAcqManager;
import edu.bpl.pwsplugin.acquisitionManagers.fluorescence.FluorAcqManager;
import edu.bpl.pwsplugin.acquisitionManagers.fluorescence.LCTFFluorAcqManager;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.org.json.JSONObject;
import org.micromanager.internal.utils.ReportingUtils;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;

public class AcqManager { // A parent acquisition manager that can direct commands down to more specific acquisition managers.
    private final PWSAcqManager pwsManager_ = new PWSAcqManager(new PWSAlbum("PWS"));
    private final DynAcqManager dynManager_ = new DynAcqManager(new PWSAlbum("Dynamics"));
    private FluorAcqManager flManager_ = null;
    private final LinkedBlockingQueue imageQueue = new LinkedBlockingQueue();; //This queue is used to pass images from one of the acquisition managers to the ImSaver which saves the file concurrently.
    private volatile boolean acquisitionRunning_ = false;
    private int cellNum_;
    private String savePath_;
    HWConfigurationSettings config;
    
    public void acquirePWS() {
        run(pwsManager_);
    }
    
    public void acquireDynamics() {
        run(dynManager_);
    }
    
    public void acquireFluorescence() {
        run(flManager_);
    }
    
    public boolean isAcquisitionRunning() {
        return acquisitionRunning_;
    }
    
    public void setCellNum(int num) {
        cellNum_ = num;
    }
    
    public void setSavePath(String savePath) {
        savePath_ = savePath;
    }
    
    public void setPWSSettings(PWSSettings settings) throws Exception {
        pwsManager_.setSequenceSettings(settings);
    }
    
    public void setDynamicsSettings(DynSettings settings) {
        dynManager_.setSequenceSettings(settings);
    }
    
    public void setFluorescenceSettings(FluorSettings settings) {
        if (Globals.getHardwareConfiguration().getConfigurationByName(settings.imConfigName).settings().configType == ImagingConfiguration.Types.StandardCamera) {
            //Acquire fluorescence with another camera so you don't have to go through the LCTF.
            flManager_ = new AltCamFluorAcqManager();
        } else {
            //Acquire fluorescence through the LCTF filter using the same camera.
            flManager_ = new LCTFFluorAcqManager();
        }
        flManager_.setFluorescenceSettings(settings);
    }
    
    private void run(AcquisitionManager manager) {
        if (acquisitionRunning_) {
            throw new RuntimeException("Attempting to start acquisition when acquisition is already running.");
        }
        acquisitionRunning_ = true;

        if (Globals.core().getPixelSizeUm() == 0.0) {
            ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
        }
        ImagingConfigurationSettings imConf = Globals.getHardwareConfiguration().settings.configs.get(0);
        MetadataBase metadata = new MetadataBase(imConf.camSettings.linearityPolynomial,
            Globals.getHardwareConfiguration().settings.systemName,
            imConf.camSettings.darkCounts);
        
        try {
            if (Globals.mm().live().getIsLiveModeOn()) {
                Globals.mm().live().setLiveMode(false);
            }
            if (imageQueue.size() > 0) {
                ReportingUtils.showMessage(String.format("The image queue started a new acquisition with %d images already in it! Your image file is likely corrupted. This can mean that Java has not been allocated enough heap size.", imageQueue.size()));
                imageQueue.clear();
            }
            manager.acquireImages(savePath_, cellNum_, imageQueue, metadata);
        } catch (Exception ex) {          
            ReportingUtils.logError("PWSPlugin, in AcqManager: " + ex.toString());
            ReportingUtils.showError("PWSPlugin, in AcqManager: " + ex.toString());
            imageQueue.clear();
        } finally {
            acquisitionRunning_ = false;
        }
    }
}
