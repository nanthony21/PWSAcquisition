/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fluorescence;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.org.json.JSONObject;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author LCPWS3
 */
public class AltCamFluorAcqManager extends FluorAcqManager{
    Camera camera;
    
    @Override
    public void setFluorescenceSettings(FluorSettings settings) {
        super.setFluorescenceSettings(settings);
        ImagingConfiguration imConf = Globals.instance().getHardwareConfiguration().getConfigurationByName(this.settings.imConfigName);
        this.camera = imConf.camera();
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
            if (Globals.instance().getMMConfigAdapter().autoFilterSwitching) {
                initialFilter = Globals.instance().core().getCurrentConfig("Filter");
                Globals.instance().core().setConfig("Filter", settings.filterConfigName);
                Globals.instance().core().waitForConfig("Filter", settings.filterConfigName); // Wait for the device to be ready.
            } else {
                ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
            }
        } catch (Exception e) {
            ReportingUtils.showMessage("Failed to set fluoresence filter. Cancelling.");
            return;
        }
        try {
            String origCam = Globals.instance().core().getCurrentConfig("Camera");
            camera.setExposure(settings.exposure);
            Globals.instance().core().clearCircularBuffer();
            Image img = camera.snapImage();
            metadata.put("exposure", camera.getExposure()); //This must happen after we have set our exposure.
            metadata.put("filterBlock", settings.filterConfigName);
            metadata.put("altCameraTransform", camera.getSettings().affineTransform); //A 2x3 affine transformation matrix specifying how coordinates in one camera translate to coordinates in another camera.
            Globals.instance().core().setConfig("Camera", origCam);
            Pipeline pipeline = Globals.instance().mm().data().copyApplicationPipeline(Globals.instance().mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            MMSaver imSaver = new MMSaver(fullSavePath, imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver.start();

            imSaver.setMetadata(metadata);
            imSaver.queue.put(img);
            imSaver.join();
        } catch (Exception e) {
            ReportingUtils.showError(e);
        } finally {
            if (Globals.instance().getMMConfigAdapter().autoFilterSwitching) {
                try {
                    Globals.instance().core().setConfig("Filter", initialFilter);
                    Globals.instance().core().waitForConfig("Filter", initialFilter); // Wait for the device to be ready.
                } catch (Exception e){
                    ReportingUtils.showError(e);
                }
            } else {
                ReportingUtils.showMessage("Return to the PWS filter block and click `OK`.");
            }
        }
    }
}
