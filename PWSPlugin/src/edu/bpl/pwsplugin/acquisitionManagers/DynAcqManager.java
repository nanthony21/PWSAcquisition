/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.PWSAlbum;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.Studio;

/**
 *
 * @author N2-LiveCell
 */
public class DynAcqManager implements AcquisitionManager{
    private Studio studio_;
    double exposure_;
    String filtLabel_;
    int wavelength_;
    
    public DynAcqManager(Studio studio){
        studio_ = studio;
    }
    
    public void setSequenceSettings(double exposure, String filtLabel, int wavelength) {
        exposure_ = exposure;
        filtLabel_ = filtLabel;
        wavelength_ = wavelength;
    }
    
    @Override
    public void acquireImages(PWSAlbum album, LinkedBlockingQueue imageQueue) {
        
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        
    }
    
    @Override
    public JSONObject modifyMetadata(JSONObject metadata) throws JSONException {
        
    }
    
    @Override
    public int getExpectedFrames() {
        
    }
}
