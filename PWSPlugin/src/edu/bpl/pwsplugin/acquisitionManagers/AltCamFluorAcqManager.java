/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.FluorescenceMetadata;
import edu.bpl.pwsplugin.metadata.MetadataBase;
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
class AltCamFluorAcqManager extends FluorAcqManager{
    Camera camera;
    
    @Override
    public void setFluorescenceSettings(FluorSettings settings) {
        super.setFluorescenceSettings(settings);
        ImagingConfiguration imConf = Globals.getHardwareConfiguration().getConfigurationByName(this.settings.imConfigName);
        this.camera = imConf.camera();
    }
    
    @Override
    public void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, MetadataBase metadata) {
        String fullSavePath;
        String initialFilter = "";
        try {
            fullSavePath = this.getSavePath(savePath, cellNum); //This also checks if the file already exists, throws error if it does.
        } catch (Exception e) {
            ReportingUtils.showMessage("Fluoresence save path already exists. Cancelling.");
            return;
        }
        try{
            if (Globals.getMMConfigAdapter().autoFilterSwitching) {
                initialFilter = Globals.core().getCurrentConfig("Filter");
                Globals.core().setConfig("Filter", settings.filterConfigName);
                Globals.core().waitForConfig("Filter", settings.filterConfigName); // Wait for the device to be ready.
            } else {
                ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
            }
        } catch (Exception e) {
            ReportingUtils.showMessage("Failed to set fluoresence filter. Cancelling.");
            return;
        }
        try {
            String origCam = Globals.core().getCurrentConfig("Camera");
            camera.setExposure(settings.exposure);
            Globals.core().clearCircularBuffer();
            Image img = camera.snapImage();
            FluorescenceMetadata flmd = new FluorescenceMetadata(metadata, settings.filterConfigName, camera.getExposure()); //This must happen after we have set our exposure.
            JSONObject md = flmd.toJson();
            md.put("altCameraTransform", camera.getSettings().affineTransform); //A 2x3 affine transformation matrix specifying how coordinates in one camera translate to coordinates in another camera.
            Globals.core().setConfig("Camera", origCam);
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            MMSaver imSaver = new MMSaver(fullSavePath, imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver.start();

            imSaver.setMetadata(md);
            imSaver.queue.put(img);
            imSaver.join();
        } catch (Exception e) {
            ReportingUtils.showError(e);
        } finally {
            if (Globals.getMMConfigAdapter().autoFilterSwitching) {
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
