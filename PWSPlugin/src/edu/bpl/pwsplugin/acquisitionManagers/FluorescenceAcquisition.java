/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.SaverThread;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.metadata.FluorescenceMetadata;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author LCPWS3
 */
public class FluorescenceAcquisition implements Acquisition<FluorSettings>{
    protected FluorSettings settings;
    Camera camera;
    TunableFilter tunableFilter;
    ImagingConfiguration imConf;
    PWSAlbum album;
    
    public FluorescenceAcquisition(PWSAlbum display) {
        album = display;
    }
    
    @Override
    public FileSpecs.Type getFileType() {
        return FileSpecs.Type.FLUORESCENCE;
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        int i = 0;
        Path path;
        do { //Increment the folder numbering until a nonused folder is found.
            path = FileSpecs.getCellFolderName(Paths.get(savePath), cellNum).resolve(FileSpecs.getFluorescenceSubfolderName(i));
            i++;
        } while(Files.isDirectory(path));
        return path.toString();
    }
    
    @Override
    public void setSettings(FluorSettings settings) {
        this.settings = settings;
        this.imConf = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.imConfigName);
        this.camera = imConf.camera();
        if (imConf.hasTunableFilter()) {
            this.tunableFilter = imConf.tunableFilter();
        } else {
            this.tunableFilter = null;
        }
    }
    
    @Override
    public FluorSettings getSettings() {
        return this.settings;
    }
    
    @Override
    public Integer numFrames() {
        return 1;
    }
    
    @Override
    public ImagingConfiguration getImgConfig() {
        return Globals.getHardwareConfiguration().getImagingConfigurationByName(getSettings().imConfigName);
    }
    
    @Override
    public void acquireImages(SaverThread imSaver, MetadataBase metadata) throws Exception {
        String initialFilter = ""; 
        boolean spectralMode = imConf.hasTunableFilter();
        if (Globals.getMMConfigAdapter().autoFilterSwitching) {
            initialFilter = Globals.core().getCurrentConfig("Filter");
            Globals.core().setConfig("Filter", this.settings.filterConfigName);
            Globals.core().waitForConfig("Filter", this.settings.filterConfigName); // Wait for the device to be ready.
        } else {
            ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
        }
        try {
            if (spectralMode) { 
                this.tunableFilter.setWavelength(settings.tfWavelength);
            }
            double origZ = Globals.core().getPosition(); //TODO will this work with PFS on?
            Globals.core().setPosition(origZ + settings.focusOffset);
            imSaver.start();
            this.camera.setExposure(settings.exposure);
            Globals.core().clearCircularBuffer();
            Image img = this.camera.snapImage();
            Integer wv;
            if (spectralMode) { wv = settings.tfWavelength; } else { wv = null; }
            FluorescenceMetadata flmd = new FluorescenceMetadata(metadata, settings.filterConfigName, camera.getExposure(), wv); //This must happen after we have set our exposure.
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image. 
            imSaver.setMetadata(flmd);
            album.clear(); //One day it would be nice to show multiple fluorescence images at once.
            album.addImage(img);
            imSaver.getQueue().add(img);
            Globals.core().setPosition(origZ);
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
