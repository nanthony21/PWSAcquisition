
package edu.bpl.pwsplugin.acquisitionManagers.fluorescence;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author backman05
 */
public class LCTFFluorAcqManager extends FluorAcqManager{
    double exposure_; //The camera exposure in milliseconds.
    int wavelength_; //The wavelength to acquire images at
    String flFilterBlock_; // The name of the fluorescence filter block config setting.
    PWSPluginSettings.HWConfiguration.CamSettings camera;
    
    public LCTFFluorAcqManager(PWSPluginSettings.HWConfiguration config) {
        this.camera = config.cameras.get(0); //TODO add a way to choose whic caamera to use.
    }
    
    @Override
    public void setFluorescenceSettings(PWSPluginSettings.FluorSettings settings) {
        //TODO refactor out all these variables and just use the `settings`
        super.setFluorescenceSettings(settings);
        flFilterBlock_ = settings.filterConfigName;
        exposure_ = settings.exposure;
        wavelength_ = settings.tfWavelength;
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
            if (Globals.getMMConfigAdapter().autoFilterSwitching) {
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
            camera.tunableFilter.setWavelength(wavelength_);
            Globals.core().setExposure(exposure_);
            Globals.core().clearCircularBuffer();
            Globals.core().snapImage();
            Image img = Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            MMSaver imSaver = new MMSaver(fullSavePath, imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver.start();
            metadata.put("wavelength", wavelength_);
            metadata.put("exposure", Globals.core().getExposure()); //This must happen after we have set our exposure.
            metadata.put("filterBlock", flFilterBlock_);
            imSaver.setMetadata(metadata);
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
