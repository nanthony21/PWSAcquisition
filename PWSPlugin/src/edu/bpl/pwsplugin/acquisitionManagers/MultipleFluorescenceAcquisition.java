/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageSaver;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.FluorescenceMetadata;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.FluorSettings;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.data.internal.DefaultMetadata;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
class MultipleFluorescenceAcquisition extends ListAcquisitionBase<FluorSettings> { //Some weird stuff can happen with the display when different iterations have different resolutions. that's ok though.
    //Acquires multiple fluorescence images from a list of fluorescence settings.
    private FluorSettings settings;
    private ImagingConfiguration imConf;
    private String initialFilter = null; //This should only stay null if we are using manual filter switching.
    
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
    protected void finalizeAcquisitions() throws MMDeviceException {
        String fluorConfigGroup = imConf.getFluorescenceConfigGroup();
        if (fluorConfigGroup != null) {
            try {
                Globals.core().setConfig(fluorConfigGroup, initialFilter);
                Globals.core().waitForConfig(fluorConfigGroup, initialFilter); // Wait for the device to be ready.
            } catch (Exception e) {
                throw new MMDeviceException(e);
            }
        } else {
            ReportingUtils.showMessage("Return to the initial filter block and click `OK`.");
        }
        initialFilter = null; //Reset this for the next run
    }
    
    @Override
    protected void initializeAcquisitions() throws MMDeviceException {
        String fluorConfigGroup = imConf.getFluorescenceConfigGroup();
        if (fluorConfigGroup != null) {
            try {
                initialFilter = Globals.core().getCurrentConfig(fluorConfigGroup);
            } catch (Exception e) {
                throw new MMDeviceException(e);
            }
        }
    }

    @Override
    protected void runSingleImageAcquisition(ImageSaver imSaver, MetadataBase metadata) throws Exception {
        boolean spectralMode = imConf.hasTunableFilter();
        String fluorConfigGroup = imConf.getFluorescenceConfigGroup();
        if (fluorConfigGroup != null) {
            Globals.core().setConfig(fluorConfigGroup, this.settings.filterConfigName);
            Globals.core().waitForConfig(fluorConfigGroup, this.settings.filterConfigName); // Wait for the device to be ready.
        } else {
            ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
        }
        if (spectralMode) { 
            imConf.tunableFilter().setWavelength(settings.tfWavelength);
        }
        imConf.zStage().setPosRelativeUm(settings.focusOffset);
        try {
            imSaver.beginSavingThread();
            imConf.camera().setExposure(settings.exposure);
            Globals.core().clearCircularBuffer();
            Image img = imConf.camera().snapImage();
            metadata.setMicroManagerMetadata((DefaultMetadata) img.getMetadata());
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
        } finally {
            imConf.zStage().setPosRelativeUm(-settings.focusOffset);
        }
    }
}
