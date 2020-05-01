
package edu.bpl.pwsplugin.acquisitionManagers.fluorescence;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.org.json.JSONObject;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author backman05
 */
public class LCTFFluorAcqManager extends FluorAcqManager{
    Camera camera;
    TunableFilter tunableFilter;
    
    @Override
    public void setFluorescenceSettings(FluorSettings settings) {
        super.setFluorescenceSettings(settings);
        ImagingConfiguration imConf = Globals.getHardwareConfiguration().getConfigurationByName(this.settings.imConfigName);
        this.camera = imConf.camera();
        this.tunableFilter = imConf.tunableFilter();
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
                Globals.core().setConfig("Filter", this.settings.filterConfigName);
                Globals.core().waitForConfig("Filter", this.settings.filterConfigName); // Wait for the device to be ready.
            } else {
                ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
            }
        } catch (Exception e) {
            ReportingUtils.showMessage("Failed to set fluoresence filter. Cancelling.");
            return;
        }
        try {
            this.tunableFilter.setWavelength(settings.tfWavelength);
            this.camera.setExposure(settings.exposure);
            Globals.core().clearCircularBuffer();
            Image img = this.camera.snapImage();
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            MMSaver imSaver = new MMSaver(fullSavePath, imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver.start();
            metadata.put("wavelength", settings.tfWavelength);
            metadata.put("exposure", camera.getExposure()); //This must happen after we have set our exposure. We don't use the settings.exposure becuase the actual exposure may differ by a little bit.
            metadata.put("filterBlock", settings.filterConfigName);
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
