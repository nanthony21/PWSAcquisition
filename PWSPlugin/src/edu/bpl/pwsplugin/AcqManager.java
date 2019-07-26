/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionManagers.PWSAcqManager;
import edu.bpl.pwsplugin.acquisitionManagers.DynAcqManager;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author N2-LiveCell
 */


public class AcqManager {
    private PWSAcqManager pwsManager_;
    private DynAcqManager dynManager_;
    private Studio studio_;
    private LinkedBlockingQueue imageQueue = new LinkedBlockingQueue();
    private boolean acquisitionRunning_ = false;
    private int cellNum_;
    private String savePath_;
    PWSAlbum album;
    ImSaverRaw imsaver_ = null;
    
    int darkCounts_;
    double[] linearityPolynomial_;
    String sysName_;

    
    public AcqManager(Studio studio) {
        studio_ = studio;
        album = new PWSAlbum(studio_);
        pwsManager_ = new PWSAcqManager(studio_);
        dynManager_ = new DynAcqManager(studio_);
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
        return metadata;
    }
    
    public void setPWSSettings(double exposure, boolean externalTrigger, 
            boolean hardwareTrigger, int[] Wv, String filterLabel) throws Exception {
        pwsManager_.setSequenceSettings(exposure, externalTrigger, hardwareTrigger, Wv, filterLabel);
    }
    
    public void setDynamicsSettings(double exposure, String filterLabel, int wavelength) {
        dynManager_.setSequenceSettings(exposure, filterLabel, wavelength);
    }
    
    private void run(AcquisitionManager manager) {
        if (studio_.core().getPixelSizeUm() == 0.0) {
            ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
        }
        JSONObject metadata;
        try {
            metadata = generateMetadata();
            metadata = manager.modifyMetadata(metadata);
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
        try {album.clear();} catch (IOException e) {ReportingUtils.logError(e, "Error from PWSALBUM");}
        try {
            if (studio_.live().getIsLiveModeOn()) {
                studio_.live().setLiveMode(false);
            }
            if (imsaver_ != null) {//Imsaver has already being assigned meaning that the acquisition has been run before. We need to make sure that this saver finished saving before continuing.
                imsaver_.join();
                imsaver_ = null;
            }
            if (imageQueue.size() > 0) {
                ReportingUtils.showMessage(String.format("The image queue started a new acquisition with %d images already in it! Go find Nick. This can mean that Java has not been allocated enough heap size.", imageQueue.size()));
                imageQueue.clear();
            }
            String fullSavePath = manager.getSavePath(savePath_, cellNum_);
            imsaver_ = new ImSaverRaw(studio_, fullSavePath, imageQueue, metadata, manager.getExpectedFrames(), true); //TODO generalize
            imsaver_.start();
            manager.acquireImages(album, imageQueue);
        } catch (Exception ex) {          
            ReportingUtils.logError("PWSPlugin, in processor: " + ex.toString());
            imageQueue.clear();
        }
    }
}
