/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.MMSaver;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.SaverThread;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.DoubleVector;
import org.micromanager.data.Image;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
abstract class ListAcquisitionBase<S> implements Acquisition<List<S>>{
    //A base class for an acquisition that acquires from a list of settings and puts the resulting images all into a shared display.
    //Images are saved to individual numbered folders.
    private List<S> settingsList;
    private final LinkedBlockingQueue imageQueue = new LinkedBlockingQueue();
    private final PWSAlbum display;
    
    protected ListAcquisitionBase(PWSAlbum album) {
        this.display = album;
    }

    
    @Override
    public void acquireImages(String savePath, int cellNum) throws Exception {
        this.display.clear(); //The implementation of `runSingleImageAcquisition` call `displayImage` to add images to the display throughout the imaging process.
        for (int i=0; i< this.settingsList.size(); i++) {
            S settings = this.settingsList.get(i);
            this.setCurrentSettings(settings);
            ImagingConfiguration imConf = this.getImgConfig(); //Activation must occur every time the imaging configuration changes. Initializemetadata requires that the correct configuration is active.
            if (!imConf.isActive()) { //It's important that the configuration is activated before we try pulling metadata like the affine transform
                imConf.activateConfiguration(); //Activation must occur every time the imaging configuration changes.
            }
            MetadataBase md = this.initializeMetadata(imConf);
            if (imageQueue.size() > 0) {
                ReportingUtils.showMessage(String.format("The image queue started a new acquisition with %d images already in it! Your image file is likely corrupted. This can mean that Java has not been allocated enough heap size.", imageQueue.size()));
                imageQueue.clear();
            }
            String subFolderName = String.format("%s_%d", FileSpecs.getSubfolderName(this.getFileType()), i);
            Path fullSavePath = FileSpecs.getCellFolderName(Paths.get(savePath), cellNum).resolve(subFolderName);
            SaverThread imSaver = new MMSaver(fullSavePath.toString(), imageQueue, this.numFrames(), FileSpecs.getFilePrefix(this.getFileType()));
            this.runSingleImageAcquisition(imSaver, md);
        }
    }
    
    @Override
    public void setSettings(List<S> settingList) {
        this.settingsList = settingList;
    }
    
    
    private MetadataBase initializeMetadata(ImagingConfiguration imConf) throws Exception {
        if (Globals.core().getPixelSizeUm() == 0.0) { //This information gets saved to the metadata below in the form of an affine transform.
            ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
        }
        DoubleVector aff = Globals.core().getPixelSizeAffine();
        List<Double> trans = new ArrayList<>();
        for (int i=0; i<aff.size(); i++) {
            trans.add(aff.get(i));
        }            

        MetadataBase metadata = new MetadataBase(
                imConf.camera().getSettings().linearityPolynomial,
                Globals.getHardwareConfiguration().getSettings().systemName,
                imConf.camera().getSettings().darkCounts,
                trans);
        return metadata;
    }
    
    protected abstract void setCurrentSettings(S settings);
    protected abstract ImagingConfiguration getImgConfig();
    protected abstract void runSingleImageAcquisition(SaverThread saver, MetadataBase md) throws Exception;
    protected abstract FileSpecs.Type getFileType(); //Return the type enumerator for this acquisition, used for file saving information.
    protected abstract Integer numFrames();
    
    protected void displayImage(Image img) { //Call this from within the implementation to add images to the display.
        this.display.addImage(img); 
    }
}
