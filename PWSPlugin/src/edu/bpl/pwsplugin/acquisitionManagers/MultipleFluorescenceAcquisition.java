/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageSaver;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.SaverThread;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.FluorescenceMetadata;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.FluorSettings;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
class MultipleFluorescenceAcquisition extends ListAcquisitionBase<FluorSettings> { //TODO some weird stuff can happen with the display when different iterations have different resolutions. that's ok though.
    //Acquires multiple fluorescence images from a list of fluorescence settings.
    private FluorSettings settings;
    private ImagingConfiguration imConf;
    
    public MultipleFluorescenceAcquisition(PWSAlbum display) {
        super(display);
    }
    
    @Override
    protected void setCurrentSettings(FluorSettings settings) {
        this.settings = settings;
        this.imConf = Globals.getHardwareConfiguration().getImagingConfigurationByName(this.settings.imConfigName);
    }
    
    
    @Override
    protected Integer numFrames() {
        return 1;
    }
    
    @Override
    protected ImagingConfiguration getImgConfig() {
        return this.imConf;
    }
    
    @Override
    protected FileSpecs.Type getFileType() {
        return FileSpecs.Type.FLUORESCENCE;
    }

    @Override
    protected void runSingleImageAcquisition(ImageSaver imSaver, MetadataBase metadata) throws Exception {
        String initialFilter = ""; 
        boolean spectralMode = imConf.hasTunableFilter();
        if (Globals.getMMConfigAdapter().autoFilterSwitching) {
            initialFilter = Globals.core().getCurrentConfig("Filter");
            Globals.core().setConfig("Filter", this.settings.filterConfigName);
            Globals.core().waitForConfig("Filter", this.settings.filterConfigName); // Wait for the device to be ready.
        } else {
            ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`."); //This blocks until saving is done.
        }
        try {
            if (spectralMode) { 
                imConf.tunableFilter().setWavelength(settings.tfWavelength);
            }
            double origZ = Globals.core().getPosition(); //TODO will this work with PFS on?
            Globals.core().setPosition(origZ + settings.focusOffset);
            imSaver.beginSavingThread();
            imConf.camera().setExposure(settings.exposure);
            Globals.core().clearCircularBuffer();
            Image img = imConf.camera().snapImage();
            Integer wv;
            if (spectralMode) { wv = settings.tfWavelength; } else { wv = null; }
            FluorescenceMetadata flmd = new FluorescenceMetadata(metadata, settings.filterConfigName, imConf.camera().getExposure(), wv); //This must happen after we have set our exposure.
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image. 
            imSaver.setMetadata(flmd);
            this.displayImage(img);
            imSaver.addImage(img);
            Globals.core().setPosition(origZ);
            imSaver.awaitThreadTermination();
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
