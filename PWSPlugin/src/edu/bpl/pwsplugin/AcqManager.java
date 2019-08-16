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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;


public class AcqManager { // A parent acquisition manager that can direct commands down to more specific acquisition managers.
    private final PWSAcqManager pwsManager_;
    private final DynAcqManager dynManager_;
    private final Studio studio_;
    private final LinkedBlockingQueue imageQueue; //This queue is used to pass images from one of the acquisition managers to the ImSaver which saves the file concurrently.
    private volatile boolean acquisitionRunning_ = false;
    private int cellNum_;
    private String savePath_;
    PWSAlbum album;
    PWSAlbum dynAlbum;
    ImSaverRaw imsaver_ = null;
    
    int darkCounts_;
    double[] linearityPolynomial_;
    String sysName_;

    
    public AcqManager(Studio studio) {
        studio_ = studio;
        album = new PWSAlbum(studio_);
        dynAlbum = new PWSAlbum(studio_);
        pwsManager_ = new PWSAcqManager(studio_, album);
        dynManager_ = new DynAcqManager(studio_, dynAlbum);
        imageQueue = new LinkedBlockingQueue();
    }
    
    public void acquirePWS() {
        if (!acquisitionRunning_) {
            acquisitionRunning_ = true;
            run(pwsManager_);
            acquisitionRunning_ = false;
        }
    }
    
    public void acquireDynamics() {
        if (!acquisitionRunning_) {
            acquisitionRunning_ = true;
            run(dynManager_);
            acquisitionRunning_ = false;
        }
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
    
    public void setSystemSettings(int darkcounts, double[] linearPoly, String sysName) {
        darkCounts_ = darkcounts;
        linearityPolynomial_ = linearPoly;
        sysName_ = sysName;
    }
    
    private JSONObject generateMetadata() throws JSONException {
        JSONObject metadata = new JSONObject();
        JSONArray linPoly;
        if (linearityPolynomial_.length > 0) {
            linPoly = new JSONArray();
            for (int i=0; i<linearityPolynomial_.length; i++) {
                linPoly.put(linearityPolynomial_[i]);
            }
            metadata.put("linearityPoly", linPoly);
        } else{
            metadata.put("linearityPoly", JSONObject.NULL);
        }
        metadata.put("system", sysName_);
        metadata.put("darkCounts", darkCounts_);
        metadata.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        return metadata;
    }
    
    public void setPWSSettings(double exposure, boolean externalTrigger, 
            boolean hardwareTrigger, int[] Wv, String filterLabel) throws Exception {
        pwsManager_.setSequenceSettings(exposure, externalTrigger, hardwareTrigger, Wv, filterLabel);
    }
    
    public void setDynamicsSettings(double exposure, String filterLabel, int wavelength, int numFrames) {
        dynManager_.setSequenceSettings(exposure, filterLabel, wavelength, numFrames);
    }
    
    private void run(AcquisitionManager manager) {
        if (studio_.core().getPixelSizeUm() == 0.0) {
            ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
        }
        JSONObject metadata;
        try {
            metadata = generateMetadata();
            if (metadata.get("system").equals("")) {
                ReportingUtils.showMessage("The `system` metadata field is blank. It should contain the name of the system.");
            }
            if (metadata.get("darkCounts").equals(0)) {
                ReportingUtils.showMessage("The `darkCounts` field of the metadata is 0. This can't be right.");
            }
        } catch (JSONException e){
            ReportingUtils.showError(e);
            return;
        }
        
        try {
            if (studio_.live().getIsLiveModeOn()) {
                studio_.live().setLiveMode(false);
            }
            if (imsaver_ != null) {//Imsaver has already being assigned meaning that the acquisition has been run before. We need to make sure that this saver finished saving before continuing.
                imsaver_.join();
                imsaver_ = null;
            }
            if (imageQueue.size() > 0) {
                ReportingUtils.showMessage(String.format("The image queue started a new acquisition with %d images already in it! This can mean that Java has not been allocated enough heap size.", imageQueue.size()));
                imageQueue.clear();
            }
            String fullSavePath = manager.getSavePath(savePath_, cellNum_);
            imsaver_ = new ImSaverRaw(studio_, fullSavePath, imageQueue, manager.getExpectedFrames(), true, manager.getFilePrefix());
            imsaver_.start();
            manager.acquireImages(imsaver_, metadata);
        } catch (Exception ex) {          
            ReportingUtils.logError("PWSPlugin, in AcqManager: " + ex.toString());
            ReportingUtils.showError("PWSPlugin, in AcqManager: " + ex.toString());
            imageQueue.clear();
        }
    }
}
