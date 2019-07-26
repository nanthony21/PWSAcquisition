/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.acquisitionManagers.PWSAcqManager;
import edu.bpl.pwsplugin.acquisitionManagers.DynAcqManager;
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
    private boolean acquisitionRunning_ = false;
    private int cellNum_;
    private String savePath_;
    JSONObject metadata = new JSONObject(); // TODO Create a whole new one for each image
    PWSAlbum album;
    
    public AcqManager(Studio studio) {
        studio_ = studio;
        album = new PWSAlbum(studio_);
        pwsManager_ = new PWSAcqManager(studio_);
        dynManager_ = new DynAcqManager(studio_);
    }
    
    public void acquirePWS() {
        if (!acquisitionRunning_) {
            acquisitionRunning_ = true;
            checkMetadata();
            pwsManager_.run(cellNum_, savePath_, album);
            acquisitionRunning_ = false;
        }
    }
    
    public void acquireDynamics() {
        if (!acquisitionRunning_) {
            acquisitionRunning_ = true;
            checkMetadata();
            dynManager_.run(cellNum_, savePath_, album);
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
    
    public void setSystemSettings(int darkcounts, double[] linearPoly, String sysName) throws JSONException {
        JSONArray linPoly;
        if (linearPoly.length > 0) {
            linPoly = new JSONArray();
            for (int i=0; i<linearPoly.length; i++) {
                linPoly.put(linearPoly[i]);
            }
            metadata.put("linearityPoly", linPoly);
        } else{
            metadata.put("linearityPoly", JSONObject.NULL);
        }
        metadata.put("system", sysName);
        metadata.put("darkCounts", darkcounts);    
    }
    
    public void setPWSSettings(double exposure, boolean externalTrigger, 
            boolean hardwareTrigger, int[] Wv, String filterLabel) throws Exception {
        pwsManager_.setSequenceSettings(exposure, externalTrigger, hardwareTrigger, Wv, filterLabel, metadata);
    }
    
    public void setDynamicsSettings(double exposure) {
        dynManager_.setSequenceSettings(exposure);
    }
    
    private void checkMetadata() throws JSONException {
            if (studio_.core().getPixelSizeUm() == 0.0) {
                ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
            }
            if (metadata.get("system").equals("")) {
                ReportingUtils.showMessage("The `system` metadata field is blank. It should contain the name of the system.");
            }
            if (metadata.get("darkCounts").equals(0)) {
                ReportingUtils.showMessage("The `darkCounts` field of the metadata is 0. This can't be right.");
            }
    }
}
