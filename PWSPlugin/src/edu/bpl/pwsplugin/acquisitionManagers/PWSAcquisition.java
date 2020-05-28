///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2019
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//

package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.MMSaver;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.configurations.SpectralCamera;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.metadata.PWSMetadata;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import org.micromanager.internal.utils.ReportingUtils;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.data.Image;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.micromanager.data.Coords;
import org.micromanager.data.Pipeline;
import org.micromanager.data.PipelineErrorException;


class PWSAcquisition implements Acquisition<PWSSettings>{
    int[] wv; //The array of wavelengths to image at.
    final String filtProp  = "Wavelength"; //The property name of the filter that we want to tune.
    Boolean hardwareSequence; // Whether or not to attempt to use TTL triggering between the camera and spectral filter.
    Boolean useExternalTrigger; // Whether or not to let the spectral filter TTL trigger a new camera frame when it is done tuning.
    double exposure_; // The camera exposure.
    PWSAlbum album_;
    SpectralCamera conf;
    PWSSettings settings;
    
    public PWSAcquisition(PWSAlbum album) {
        album_ = album;
    }
    
    @Override
    public void setSettings(PWSSettings settings) {
        this.settings = settings;
        conf = (SpectralCamera) Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.imConfigName);
        TunableFilter filter = conf.tunableFilter();
        exposure_ = settings.exposure;
        useExternalTrigger = settings.externalCamTriggering;
        wv = settings.getWavelengthArray();
        hardwareSequence =  settings.ttlTriggering;           
        
        if (hardwareSequence) {
            try {
                if (!filter.supportsSequencing()) {
                    throw new UnsupportedOperationException("The filter device does not support hardware TTL sequencing.");
                }
                if (filter.getMaxSequenceLength() < wv.length) {
                    throw new UnsupportedOperationException("The filter device does not support sequencing as many wavelengths as have been specified. Max is " + filter.getMaxSequenceLength());
                }
                filter.loadSequence(wv);
            } catch (MMDeviceException e) {
                throw new RuntimeException(e);
            }
        }  
    }
    
    @Override
    public PWSSettings getSettings() {
        return this.settings;
    }
    
    @Override
    public String getFilePrefix() {
        return "pws";
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException{
        Path path = Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)).resolve("PWS");
        if (Files.isDirectory(path)){
            throw new FileAlreadyExistsException("Cell " + cellNum + " PWS already exists.");
        } 
        return path.toString();
    }
      
    @Override
    public void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, MetadataBase metadata) throws Exception {
        long configStartTime = System.currentTimeMillis();
        album_.clear();
        int initialWv = 550;

        try {    
            if (!conf.isActive()) {
                conf.activateConfiguration();
            }
            initialWv = conf.tunableFilter().getWavelength(); //Get initial wavelength
            Globals.core().clearCircularBuffer();     
            conf.camera().setExposure(exposure_);
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true);
            
            //Prepare metadata and start imsaver
            List<Double> WV = new ArrayList<>();
            for (int i = 0; i < wv.length; i++) {
                WV.add(Double.valueOf(wv[i]));
            }     
            PWSMetadata pmd = new PWSMetadata(metadata, WV, conf.camera().getExposure());  //This must happen after we have set the camera to our desired exposure.
            MMSaver imSaver_ = new MMSaver(this.getSavePath(savePath, cellNum), imagequeue, wv.length, this.getFilePrefix());
            imSaver_.setMetadata(pmd.toJson());
            imSaver_.start();
            
            long seqEndTime=0;
            long collectionEndTime=0;
            long seqStartTime=0;
            if (hardwareSequence) {
                try {
                    double delayMs = conf.tunableFilter().getDelayMs(); //Use the delay defined by the tunable filter's device adapter.
                    conf.startTTLSequence(this.wv.length, delayMs, useExternalTrigger);
                    seqStartTime = System.currentTimeMillis();
                    
                    boolean canExit = false;
                    int i = 0;
                    int oldi = -1;
                    long lastImTime = System.currentTimeMillis();
                    while (true) {
                        boolean remaining = (Globals.core().getRemainingImageCount() > 0);
                        boolean running = (Globals.core().isSequenceRunning(conf.camera().getName()));
                        if ((!remaining) && (canExit)) {
                            break;  //Everything is taken care of.
                        }
                        if (remaining) {    //Process images
                            Image im = Globals.mm().data().convertTaggedImage(Globals.core().popNextTaggedImage());
                            addImage(im, i, album_, pipeline, imSaver_.queue);
                            i++;
                            lastImTime = System.currentTimeMillis();
                            collectionEndTime = System.currentTimeMillis();
                        }
                        if ((System.currentTimeMillis() - lastImTime) > 10000) { //Check for timeout if for some reason the acquisition is stalled.
                            seqEndTime = System.currentTimeMillis();
                            ReportingUtils.showError("PWSAcquisition timed out while waiting for images from camera.");
                            canExit = true;
                        }
                        if (!running) {
                            seqEndTime = System.currentTimeMillis();
                            canExit = true;
                        }
                    }
                } finally {
                    conf.stopTTLSequence(); //Got to make sure to stop the sequencing behaviour.         
                    String timeMsg = "PWSPlugin: Hardware Sequenced Acq: ConfigurationTime:" + (seqStartTime-configStartTime)/1000.0 + "HWAcqTime:"+(seqEndTime-seqStartTime)/1000.0+"ImgCollectionTime:"+(collectionEndTime-seqEndTime)/1000.0;
                    ReportingUtils.logMessage(timeMsg);
                }
            }
            else {  //Software sequenced acquisition
                for (int i=0; i<wv.length; i++) {
                    Image im = conf.snapImage(wv[i]);
                    addImage(im, i, album_, pipeline, imSaver_.queue);
                }
            }
            imSaver_.join();
        } finally {
            conf.tunableFilter().setWavelength(initialWv); //Set back to initial wavelength
        }
    }
    
    private void addImage(Image im, int idx, PWSAlbum album, Pipeline pipeline, LinkedBlockingQueue imageQueue) throws IOException, PipelineErrorException{
        Coords newCoords = im.getCoords().copyBuilder().t(idx).build();
        im = im.copyAtCoords(newCoords);
        pipeline.insertImage(im); //Add image to the data pipeline for processing
        im = pipeline.getDatastore().getImage(newCoords); //Retrieve the processed image.
        album.addImage(im); //Add the image to the album for display
        imageQueue.add(im); //Add the image to a queue for multithreaded saving.
    }
}