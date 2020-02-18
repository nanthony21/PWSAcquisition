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
import edu.bpl.pwsplugin.PWSAlbum;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import org.micromanager.internal.utils.ReportingUtils;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.data.Image;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;
import org.micromanager.data.Coords;
import org.micromanager.data.Pipeline;
import org.micromanager.data.PipelineErrorException;


public class PWSAcqManager implements AcquisitionManager{
    int[] wv; //The array of wavelengths to image at.
    final String filtProp  = "Wavelength"; //The property name of the filter that we want to tune.
    Boolean hardwareSequence; // Whether or not to attempt to use TTL triggering between the camera and spectral filter.
    Boolean useExternalTrigger; // Whether or not to let the spectral filter TTL trigger a new camera frame when it is done tuning.
    double exposure_; // The camera exposure.
    PWSAlbum album_;
    PWSPluginSettings.HWConfiguration config;
    
    public PWSAcqManager(PWSAlbum album, PWSPluginSettings.HWConfiguration config) {
        album_ = album;
        this.config = config;
    }
    
    public void setSequenceSettings(PWSPluginSettings.PWSSettings settings) throws Exception {
        PWSPluginSettings.HWConfiguration.CamSettings camera = this.config.cameras.get(0);
        exposure_ = settings.exposure;
        useExternalTrigger = settings.externalCamTriggering;
        wv = settings.getWavelengthArray();
        hardwareSequence =  settings.ttlTriggering;           
        
        if (hardwareSequence) {
            if (!camera.tunableFilter.supportsSequencing()) {
                throw new Exception("The filter device does not support hardware TTL sequencing.");
            }
            if (camera.tunableFilter.getMaxSequenceLength() < wv.length) {
                throw new Exception("The filter device does not support sequencing as many wavelengths as have been specified. Max is " + camera.tunableFilter.getMaxSequenceLength());
            }
            camera.tunableFilter.loadSequence(wv);
        }  
    }
    
    @Override
    public int getExpectedFrames() {
        return wv.length;
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
    public void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, JSONObject metadata) {
        long configStartTime = System.currentTimeMillis();
        try {album_.clear();} catch (IOException e) {ReportingUtils.logError(e, "Error from PWSALBUM");}
        int initialWv = 550;
        PWSPluginSettings.HWConfiguration.CamSettings camera = this.config.cameras.get(0);
        try {    
            initialWv = camera.tunableFilter.getWavelength(); //Get initial wavelength
            String cam = Globals.core().getCameraDevice();
            Globals.core().waitForDevice(cam);
            Globals.core().clearCircularBuffer();     
            Globals.core().setExposure(cam, exposure_);
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true);
            
            //Prepare metadata and start imsaver
            JSONArray WV = new JSONArray();
            for (int i = 0; i < wv.length; i++) {
                WV.put(wv[i]);
            }        
            metadata.put("wavelengths", WV);
            metadata.put("exposure", Globals.core().getExposure()); //This must happen after we have set the camera to our desired exposure.
            MMSaver imSaver_ = new MMSaver(this.getSavePath(savePath, cellNum), imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver_.setMetadata(metadata);
            imSaver_.start();
            
            long seqEndTime=0;
            long collectionEndTime=0;
            long seqStartTime=0;
            if (hardwareSequence) {
                String origCameraTrigger="";  
                if (Globals.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")){
                    origCameraTrigger = Globals.core().getProperty(cam, "TRIGGER SOURCE");
                }
                try {
                    double delayMs = camera.tunableFilter.getDelayMs(); //Use the delay defined by the tunable filter's device adapter.
                    if (useExternalTrigger) {
                        if (Globals.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")) { 
                            Globals.core().setProperty(cam, "TRIGGER SOURCE", "EXTERNAL");
                            Globals.core().setProperty(cam, "TRIGGER DELAY", delayMs/1000); //This is in units of seconds.
                            Globals.core().startSequenceAcquisition(wv.length, 0, false); //The hamamatsu adapter throws an eror if the interval is not 0.
                            camera.tunableFilter.startSequence(); //This should trigger a pulse which sets the whole thing off.
                        }   
                    }
                    else { //Since we're not using an external trigger we need to have the camera control the timing.
                        double exposurems = Globals.core().getExposure();
                        double readoutms = 12; //This is based on the frame rate calculation portion of the 13440-20CU camera. 9.7 us per line, reading two lines at once, 2048 lines -> 0.097*2048/2 ~= 10 ms. However testing has shown if we set this exactly then we end up missing every other frame and getting half our frame rate add a buffer of 2ms to be safe.
                        double intervalMs = (exposurems+readoutms+delayMs);
                        camera.tunableFilter.startSequence();
                        if (Globals.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")) { //This device adapter doesn't seem to support delays in the sequence acquisition. We instead set the master pulse interval.
                            Globals.core().setProperty(cam, "TRIGGER SOURCE", "MASTER PULSE"); //Make sure that Master Pulse is triggering the camera.
                            Globals.core().setProperty(cam, "MASTER PULSE INTERVAL", intervalMs/1000.0); //In units of seconds
                            Globals.core().startSequenceAcquisition(wv.length, 0, false); //The hamamatsu adapter throws an error if the interval is not 0.
                            seqStartTime = System.currentTimeMillis();
                        } else{
                            Globals.core().startSequenceAcquisition(wv.length, intervalMs, false); //Supposedly having a non-zero interval acqually only works for Andor cameras.
                            seqStartTime = System.currentTimeMillis();
                        }
                    }
                    boolean canExit = false;
                    int i = 0;
                    int oldi = -1;
                    long lastImTime = System.currentTimeMillis();
                    while (true) {
                        boolean remaining = (Globals.core().getRemainingImageCount() > 0);
                        boolean running = (Globals.core().isSequenceRunning(cam));
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
                }
                finally {
                    Globals.core().stopSequenceAcquisition();
                    if (Globals.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")){
                        Globals.core().setProperty(cam, "TRIGGER SOURCE", origCameraTrigger); //Set the trigger source back ot what it was originally
                    }
                    try {
                        camera.tunableFilter.stopSequence();//Got to make sure to stop the sequencing behaviour.
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ReportingUtils.logMessage("ERROR: PWSPlugin: Stopping property sequence: " + ex.getMessage());
                    }
                    String timeMsg = "PWSPlugin: Hardware Sequenced Acq: ConfigurationTime:" + (seqStartTime-configStartTime)/1000.0 + "HWAcqTime:"+(seqEndTime-seqStartTime)/1000.0+"ImgCollectionTime:"+(collectionEndTime-seqEndTime)/1000.0;
                    ReportingUtils.logMessage(timeMsg);
                }
            }
            else {  //Software sequenced acquisition
                for (int i=0; i<wv.length; i++) {
                    camera.tunableFilter.setWavelength(wv[i]);
                    while (camera.tunableFilter.isBusy()) {Thread.sleep(1);} //Wait until the device says it is tuned.
                    Globals.core().snapImage();
                    Image im = Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
                    addImage(im, i, album_, pipeline, imSaver_.queue);
                }
            }
            imSaver_.join();
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logMessage("ERROR: PWSPlugin: " + ex.getMessage());
        } finally {
            try{
                camera.tunableFilter.setWavelength(initialWv); //Set back to initial wavelength
            } catch (Exception ex) {
                ReportingUtils.showError(ex);
                ex.printStackTrace();
                ReportingUtils.logMessage("ERROR: PWSPlugin: " + ex.getMessage());
            }
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