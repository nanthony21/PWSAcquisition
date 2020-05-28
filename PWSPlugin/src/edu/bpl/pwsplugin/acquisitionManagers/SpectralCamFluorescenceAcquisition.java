
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.MMSaver;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.SaverThread;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
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
 * @author backman05
 */
class SpectralCamFluorescenceAcquisition extends FluorescenceAcquisition{
    Camera camera;
    TunableFilter tunableFilter;
    ImagingConfiguration imConf;
    
    @Override
    public void setSettings(FluorSettings settings) {
        super.setSettings(settings);
        imConf = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.imConfigName);
        this.camera = imConf.camera();
        this.tunableFilter = imConf.tunableFilter();
    }
    
    @Override
    public void acquireImages(SaverThread imSaver, int cellNum, MetadataBase metadata) throws Exception {
        String initialFilter = "";
        
        if (Globals.getMMConfigAdapter().autoFilterSwitching) {
            initialFilter = Globals.core().getCurrentConfig("Filter");
            Globals.core().setConfig("Filter", this.settings.filterConfigName);
            Globals.core().waitForConfig("Filter", this.settings.filterConfigName); // Wait for the device to be ready.
        } else {
            ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
        }
        try {
            if (!imConf.isActive()) {
                imConf.activateConfiguration();
            }
            this.tunableFilter.setWavelength(settings.tfWavelength);
            this.camera.setExposure(settings.exposure);
            Globals.core().clearCircularBuffer();
            Image img = this.camera.snapImage();
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            imSaver.start();
            FluorescenceMetadata flmd = new FluorescenceMetadata(metadata, settings.filterConfigName, camera.getExposure()); //This must happen after we have set our exposure. We don't use the settings.exposure becuase the actual exposure may differ by a little bit.
            JSONObject md = flmd.toJson();
            md.put("wavelength", settings.tfWavelength);
            imSaver.setMetadata(md);
            imSaver.getQueue().add(img);
            imSaver.join();
        } finally {
            if (Globals.getMMConfigAdapter().autoFilterSwitching) {
                Globals.core().setConfig("Filter", initialFilter);
                Globals.core().waitForConfig("Filter", initialFilter); // Wait for the device to be ready.
            } else {
                ReportingUtils.showMessage("Return to the PWS filter block and click `OK`.");
            }
        }
    }
}
