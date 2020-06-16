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
import org.micromanager.internal.utils.ReportingUtils;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DoubleVector;


public class AcquisitionManager { 
    /* A parent acquisition manager that can direct commands down to more specific acquisition managers.
    There should only be one of these objects for a given set of hardware in order to avoid trying to run multiple acquisitions at once.
    This should be the only way to access any of sublevel acquisition managers.
    */
    private final PWSAcquisition pwsManager_ = new PWSAcquisition(new PWSAlbum("PWS"));
    private final DynamicsAcquisition dynManager_ = new DynamicsAcquisition(new PWSAlbum("Dynamics"));
    private final FluorescenceAcquisition flManager_ = new FluorescenceAcquisition(new PWSAlbum("Fluorescence"));
    private volatile boolean acquisitionRunning_ = false;
    private int cellNum_;
    private String savePath_;
    
    private void run(Acquisition manager) throws InterruptedException, Exception { //All acquisitions should be run with this.
        if (acquisitionRunning_) {
            throw new IllegalStateException("Attempting to start acquisition when acquisition is already running.");
        }
        acquisitionRunning_ = true;

        if (Globals.core().getPixelSizeUm() == 0.0) { //This information gets saved to the metadata below in the form of an affine transform.
            ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
        }
        try {
            ImagingConfiguration imConf = manager.getImgConfig(); 
            if (!imConf.isActive()) { //It's important that the configuration is activated before we try pulling metadata like the affine transform
                imConf.activateConfiguration(); //Activation must occur every time the imaging configuration changes.
            }
            DoubleVector aff = Globals.core().getPixelSizeAffine();
            List<Double> trans = new ArrayList<>();
            for (int i=0; i<aff.size(); i++) {
                trans.add(aff.get(i));
            }            
     
            MetadataBase metadata = new MetadataBase(
                    imConf.camera().getSettings().linearityPolynomial,
                    Globals.getHardwareConfiguration().getSettings().systemName,
                    imConf.camera().getSettings().darkCounts,
                    trans);
        
            if (Globals.mm().live().getIsLiveModeOn()) {
                Globals.mm().live().setLiveMode(false);
            }
            manager.acquireImages(savePath_, cellNum_, metadata);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw ie;
        } finally {
            acquisitionRunning_ = false;
        }
    }
    
    public void setFluorescenceSettings(FluorSettings settings) { flManager_.setSettings(settings); }
    
    public void setCellNum(int num) { cellNum_ = num; }
    
    public void setSavePath(String savePath) { savePath_ = savePath;}
    
    public String getSavePath() { return savePath_; }
    
    public void setPWSSettings(PWSSettings settings) throws Exception { pwsManager_.setSettings(settings); }
    
    public void setDynamicsSettings(DynSettings settings) { dynManager_.setSettings(settings); }
    
    public void acquirePWS() throws InterruptedException, Exception { run(pwsManager_); }
    
    public void acquireDynamics() throws InterruptedException, Exception { run(dynManager_); }
    
    public void acquireFluorescence() throws InterruptedException, Exception { run(flManager_); }
    
    public boolean isAcquisitionRunning() { return acquisitionRunning_; }
}
