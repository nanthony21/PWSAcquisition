/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author LCPWS3
 */
public class AltCamFluorAcqManager extends FluorAcqManager{
    double exposure_; //The camera exposure in milliseconds.
    String flFilterBlock_; // The name of the fluorescence filter block config setting.
    boolean autoFilter_; //Whether or not the device can automatically switch filter blocks or needs user input.
    String flCam_; //The name of the config in the "Camera" config group that switches to fluoresence mode.
    
    public void setFluorescenceSettings(boolean autoFilter, String flFilter, String flCamera, double exposure) {
        autoFilter_ = autoFilter;
        flFilterBlock_ = flFilter;
        flCam_ = flCamera;
        exposure_ = exposure;
    }
    
    @Override
    public void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, JSONObject metadata) {
        String fullSavePath;
        String initialFilter = "";
        try {
            fullSavePath = this.getSavePath(savePath, cellNum); //This also checks if the file already exists, throws error if it does.
        } catch (Exception e) {
            ReportingUtils.showMessage("Fluoresence save path already exists. Cancelling.");
            return;
        }
        try{
            if (autoFilter_) {
                initialFilter = Globals.core().getCurrentConfig("Filter");
                Globals.core().setConfig("Filter", flFilterBlock_);
                Globals.core().waitForConfig("Filter", flFilterBlock_); // Wait for the device to be ready.
            } else {
                ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
            }
        } catch (Exception e) {
            ReportingUtils.showMessage("Failed to set fluoresence filter. Cancelling.");
            return;
        }
        try {
            String origCam = Globals.core().getCurrentConfig("Camera");
            Globals.core().setConfig("Camera", flCam_);
            Globals.core().waitForConfig("Camera", flCam_);
            Globals.core().setExposure(exposure_);
            Globals.core().clearCircularBuffer();
            Globals.core().snapImage();
            Globals.core().setConfig("Camera", origCam);
            Image img = Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            MMSaver imSaver = new MMSaver(fullSavePath, imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver.start();
            metadata.put("exposure", Globals.core().getExposure()); //This must happen after we have set our exposure.
            metadata.put("filterBlock", flFilterBlock_);
            imSaver.setMetadata(metadata);
            imSaver.queue.put(img);
            imSaver.join();
            Globals.core().waitForConfig("Camera", origCam);
        } catch (Exception e) {
            ReportingUtils.showError(e);
        } finally {
            if (autoFilter_) {
                try {
                    Globals.core().setConfig("Filter", initialFilter);
                    Globals.core().waitForConfig("Filter", initialFilter); // Wait for the device to be ready.
                } catch (Exception e){
                    ReportingUtils.showError(e);
                }
            } else {
                ReportingUtils.showMessage("Return to the PWS filter block and click `OK`.");
            }
        }
    }
}
