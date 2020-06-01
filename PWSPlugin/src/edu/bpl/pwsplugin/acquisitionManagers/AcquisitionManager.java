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
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.internal.utils.ReportingUtils;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.MMSaver;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.SaverThread;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;

public class AcquisitionManager { 
    /* A parent acquisition manager that can direct commands down to more specific acquisition managers.
    There should only be one of these objects for a given set of hardware in order to avoid trying to run multiple acquisitions at once.
    This should be the only way to access any of sublevel acquisition managers.
    */
    private final PWSAcquisition pwsManager_ = new PWSAcquisition(new PWSAlbum("PWS"));
    private final DynamicsAcquisition dynManager_ = new DynamicsAcquisition(new PWSAlbum("Dynamics"));
    private final FluorescenceAcquisition flManager_ = new FluorescenceAcquisition(new PWSAlbum("Fluorescence"));
    private final LinkedBlockingQueue imageQueue = new LinkedBlockingQueue();; //This queue is used to pass images from one of the acquisition managers to the ImSaver which saves the file concurrently.
    private volatile boolean acquisitionRunning_ = false;
    private int cellNum_;
    private String savePath_;
    
    private void run(Acquisition manager) throws InterruptedException {
        if (acquisitionRunning_) {
            throw new RuntimeException("Attempting to start acquisition when acquisition is already running.");
        }
        acquisitionRunning_ = true;

        if (Globals.core().getPixelSizeUm() == 0.0) { //TODO bundle this into the `Metadata`
            ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
        }
        ImagingConfigurationSettings imConf = Globals.getHardwareConfiguration().getSettings().configs.get(0);
  
        MetadataBase metadata = new MetadataBase(imConf.camSettings.linearityPolynomial,
            Globals.getHardwareConfiguration().getSettings().systemName,
            imConf.camSettings.darkCounts);
        
        try {
            if (Globals.mm().live().getIsLiveModeOn()) {
                Globals.mm().live().setLiveMode(false);
            }
            if (imageQueue.size() > 0) {
                ReportingUtils.showMessage(String.format("The image queue started a new acquisition with %d images already in it! Your image file is likely corrupted. This can mean that Java has not been allocated enough heap size.", imageQueue.size()));
                imageQueue.clear();
            }
            SaverThread imSaver = new MMSaver(manager.getSavePath(savePath_, cellNum_), imageQueue, manager.numFrames() ,manager.getFilePrefix());
            manager.acquireImages(imSaver, metadata);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw ie;
            //throw new RuntimeException(ie);
        } catch (Exception ex) {  
            throw new RuntimeException(ex);
        } finally {
            imageQueue.clear();
            acquisitionRunning_ = false;
        }
    }
    
    public void setFluorescenceSettings(FluorSettings settings) { flManager_.setSettings(settings); }
    
    public void setCellNum(int num) { cellNum_ = num; }
    
    public void setSavePath(String savePath) { savePath_ = savePath;}
    
    public void setPWSSettings(PWSSettings settings) throws Exception { pwsManager_.setSettings(settings); }
    
    public void setDynamicsSettings(DynSettings settings) { dynManager_.setSettings(settings); }
    
    public PWSSettings getPWSSettings() { return pwsManager_.getSettings(); }
    public DynSettings getDynSettings() { return dynManager_.getSettings(); }
    public FluorSettings getFluorescenceSettings() { return flManager_.getSettings(); }
    
    public void acquirePWS() throws InterruptedException { run(pwsManager_); }
    
    public void acquireDynamics() throws InterruptedException { run(dynManager_); }
    
    public void acquireFluorescence() throws InterruptedException { run(flManager_); }
    
    public boolean isAcquisitionRunning() { return acquisitionRunning_; }
}
