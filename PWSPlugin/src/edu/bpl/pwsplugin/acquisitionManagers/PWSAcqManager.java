package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.PWSAlbum;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import org.micromanager.internal.utils.ReportingUtils;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.StrVector;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.data.Coords;
import org.micromanager.data.Pipeline;
import org.micromanager.data.PipelineErrorException;



public class PWSAcqManager implements AcquisitionManager{
    Studio studio_;
    Image[] imageArray;
    int[] wv;
    String filtLabel;
    final String filtProp  = "Wavelength";
    Boolean hardwareSequence;
    Boolean useExternalTrigger;
    double exposure_;
    
    public PWSAcqManager(Studio studio) {
        studio_ = studio;
    }
    
    public void setSequenceSettings(double exposure, boolean externalTrigger, 
            boolean hardwareTrigger, int[] Wv, String filterLabel) throws Exception {
        exposure_ = exposure;
        useExternalTrigger = externalTrigger;
        wv = Wv;
        filtLabel = filterLabel;
        hardwareSequence =  hardwareTrigger;           
        
        if (hardwareSequence) {
            try {
                if (!studio_.core().isPropertySequenceable(filtLabel, filtProp)){
                    throw new Exception("The filter device does not have a sequenceable 'Wavelength' property.");
                    //ReportingUtils.showError("The filter device does not have a sequenceable 'Wavelength' property.");
                }
                if (studio_.core().getPropertySequenceMaxLength(filtLabel, filtProp) < wv.length) {
                    throw new Exception("The filter device does not support sequencing as many wavelenghts as have been specified. Max is " + Integer.toString(studio_.core().getPropertySequenceMaxLength(filtLabel, filtProp)));
                    //ReportingUtils.showError("The filter device does not support sequencing as many wavelenghts as have been specified. Max is " + Integer.toString(studio_.core().getPropertySequenceMaxLength(filtLabel, filtProp)));
                }
                StrVector strv = new StrVector();
                for (int i = 0; i < wv.length; i++) {   //Convert wv from int to string for sending to the device.
                    strv.add(String.valueOf(wv[i]));
                }
                studio_.core().loadPropertySequence(filtLabel, filtProp, strv);
            }
            catch (Exception ex) {
                ReportingUtils.showError(ex);
                throw ex;
            }
        }  
    }
    
    @Override
    public JSONObject modifyMetadata(JSONObject metadata) throws JSONException {
        JSONArray WV = new JSONArray();
        for (int i = 0; i < wv.length; i++) {
            WV.put(wv[i]);
        }        
        metadata.put("wavelengths", WV);  
        return metadata;
    }
    
    @Override
    public int getExpectedFrames() {
        return wv.length;
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException{
        if (Files.isDirectory(Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)))){
            ReportingUtils.showError("Cell " + cellNum + " already exists.");
            throw new FileAlreadyExistsException("Cell " + cellNum + " already exists.");
        } 
        return Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)).toString();
    }
      
    @Override
    public void acquireImages(PWSAlbum album, LinkedBlockingQueue imageQueue) {
        double initialWv = 550;
        try {    
            initialWv = Double.valueOf(studio_.core().getProperty(filtLabel, filtProp)); //Get initial wavelength
            String cam = studio_.core().getCameraDevice();
            studio_.core().waitForDevice(cam);
            studio_.core().clearCircularBuffer();     
            studio_.core().setExposure(cam, exposure_);
            Pipeline pipeline = studio_.data().copyApplicationPipeline(studio_.data().createRAMDatastore(), true);
            
            long now = System.currentTimeMillis();
            
            if (hardwareSequence) {
                String origCameraTrigger="";  
                if (studio_.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")){
                    origCameraTrigger = studio_.core().getProperty(cam, "TRIGGER SOURCE");
                }
                try {
                    studio_.core().startPropertySequence(filtLabel, filtProp);
                    double delayMs = studio_.core().getDeviceDelayMs(filtLabel); //Use the delay defined by the tunable filter's device adapter.
                    if (useExternalTrigger) {
                        if (studio_.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")) { 
                            studio_.core().setProperty(cam, "TRIGGER SOURCE", "EXTERNAL");
                            studio_.core().setProperty(cam, "TRIGGER_DELAY", delayMs/1000); //This is in units of seconds.
                            studio_.core().startSequenceAcquisition(wv.length, 0, false); //The hamamatsu adapter throws an eror if the interval is not 0.
                            int currWv = Integer.parseInt(studio_.core().getProperty(filtLabel, filtProp));
                            studio_.core().setProperty(filtLabel, filtProp, currWv+1); //Trigger a pulse which sets the whole thing off.
                        }   
                    }
                    else { //Since we're not using an external trigger we need to have the camera control the timing.
                        double exposurems = studio_.core().getExposure();
                        double readoutms = 10; //This is based on the frame rate calculation portion of the 13440-20CU camera. 9.7 us per line, reading two lines at once, 2048 lines -> 0.097*2048/2 ~= 10 ms
                        double intervalMs = (exposurems+readoutms+delayMs);
                        if (studio_.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")) { //This device adapter doesn't seem to support delays in the sequence acquisition. We instead set the master pulse interval.
                            studio_.core().setProperty(cam, "TRIGGER SOURCE", "MASTER PULSE"); //Make sure that Master Pulse is triggering the camera.
                            studio_.core().setProperty(cam, "MASTER PULSE INTERVAL", intervalMs/1000.0); //In units of seconds
                            studio_.core().startSequenceAcquisition(wv.length, 0, false); //The hamamatsu adapter throws an error if the interval is not 0.
                        } else{
                            studio_.core().startSequenceAcquisition(wv.length, intervalMs, false); //Supposedly having a non-zero interval acqually only works for Andor cameras.
                        }
                    }
                    
                    boolean canExit = false;
                    int i = 0;
                    while (true) {
                        boolean remaining = (studio_.core().getRemainingImageCount() > 0);
                        boolean running = (studio_.core().isSequenceRunning(cam));
                        if ((!remaining) && (canExit)) {
                            break;  //Everything is taken care of.
                        }
                        if (remaining) {    //Process images
                            Image im = studio_.data().convertTaggedImage(studio_.core().popNextTaggedImage());
                            addImage(im, i, album, pipeline, imageQueue);
                            i++;
                        }
                        if (!running) {
                            canExit = true;
                        }
                     }
                }
                finally {
                    studio_.core().stopSequenceAcquisition();
                    if (studio_.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")){
                        studio_.core().setProperty(cam, "TRIGGER SOURCE", origCameraTrigger); //Set the trigger source back ot what it was originally
                    }
                    try {
                        studio_.core().stopPropertySequence(filtLabel, filtProp);//Got to make sure to stop the sequencing behaviour.
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ReportingUtils.logMessage("ERROR: PWSPlugin: Stopping property sequence: " + ex.getMessage());
                    }
                }
            }
            else {  //Software sequenced acquisition
                for (int i=0; i<wv.length; i++) {
                    studio_.core().setProperty(filtLabel, filtProp, wv[i]);
                    while (studio_.core().deviceBusy(filtLabel)) {Thread.sleep(1);} //Wait until the device says it is tuned.
                    studio_.core().snapImage();
                    Image im = studio_.data().convertTaggedImage(studio_.core().getTaggedImage());
                    addImage(im, i, album, pipeline, imageQueue);
                }
            }
            long itTook = System.currentTimeMillis() - now;         
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logMessage("ERROR: PWSPlugin: " + ex.getMessage());
        } finally {
            try{
                studio_.core().setProperty(filtLabel, filtProp, initialWv); //Set back to initial wavelength
            } catch (Exception ex) {
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