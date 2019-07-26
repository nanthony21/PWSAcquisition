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
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;

/**
 *
 * @author N2-LiveCell
 */
public class DynAcqManager implements AcquisitionManager{
    private Studio studio_;
    double exposure_;
    String filtLabel_;
    int wavelength_;
    int numFrames_;
    
    public DynAcqManager(Studio studio){
        studio_ = studio;
    }
    
    public void setSequenceSettings(double exposure, String filtLabel, int wavelength, int numFrames) {
        exposure_ = exposure;
        filtLabel_ = filtLabel;
        wavelength_ = wavelength;
        numFrames_ = numFrames;
    }
    
    @Override
    public void acquireImages(PWSAlbum album, LinkedBlockingQueue imageQueue) {
        studio_.core().setProperty(filtLabel_, "Wavelength", wavelength_);
        studio_.core().setExposure(exposure_);
        studio_.core().setCircularBufferMemoryFootprint(1000); //increase the circular buffer to 1gb to avoid weird issues with lost images
        studio_.core().clearCircularBuffer();
        studio_.core().startSequenceAcquisition(numFrames_, 0, false);
        Pipeline pipeline = studio_.data().copyApplicationPipeline(studio_.data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)

        for (int i=0; i<numFrames_; i++) {
            while (studio_.core().getRemainingImageCount()<1) { //Wait for an image to be ready
                pause(0.01);
            }
            Image im = studio_.data().convertTaggedImage(studio_.core().popNextTaggedImage());
            Coords newCoords = im.getCoords().copyBuilder().t(i).build();
            im = im.copyAtCoords(newCoords);
            pipeline.insertImage(im); //Add image to the data pipeline for processing
            im = pipeline.getDatastore().getImage(newCoords); //Retrieve the processed image.
            imageQueue.put(im);
        }
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
