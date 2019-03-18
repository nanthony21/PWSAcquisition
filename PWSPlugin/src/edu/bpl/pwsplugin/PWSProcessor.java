package edu.bpl.pwsplugin;

import java.io.IOException;
import org.micromanager.internal.utils.ReportingUtils;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.StrVector;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Pipeline;
import org.micromanager.data.PipelineErrorException;



public class PWSProcessor implements Runnable{
    Studio studio_;
    LinkedBlockingQueue imageQueue = new LinkedBlockingQueue();
    boolean debugLogEnabled_ = true;
    Image[] imageArray;
    int[] wv;
    String filtLabel;
    final String filtProp  = "Wavelength";
    Boolean hardwareSequence;
    Boolean useExternalTrigger;
    String savePath;
    int cellNum;
    JSONObject metadata = new JSONObject();
    PWSAlbum album;
    double exposure_;
    Pipeline pipeline_;
    
    public PWSProcessor(Studio studio) {
        studio_ = studio;
        album = new PWSAlbum(studio_);
    }
    
    public void setSaveSettings(String savepath, int cellnum) {
        savePath = savepath;
        cellNum = cellnum;
    }
    
    public void setSequenceSettings(boolean externalTrigger, 
            boolean hardwareTrigger, int[] Wv, String filterLabel) throws Exception {
        useExternalTrigger = externalTrigger;
        wv = Wv;
        filtLabel = filterLabel;
        hardwareSequence =  hardwareTrigger;
        
        JSONArray WV = new JSONArray();
        for (int i = 0; i < wv.length; i++) {
            WV.put(wv[i]);
        }        
        metadata.put("wavelengths", WV);             
        
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
    
    public void setOtherSettings(int darkcounts, double[] linearPoly, String sysName, double exposure) throws JSONException {
        JSONArray linPoly;
        if (linearPoly.length > 0) {
            linPoly = new JSONArray();
            for (int i=0; i<linearPoly.length; i++) {
                linPoly.put(linearPoly[i]);
            }
            metadata.put("linearityPoly", linPoly);
        } else{
            metadata.put("linearityPoly", JSONObject.NULL);
        }
        metadata.put("system", sysName);
        metadata.put("darkCounts", darkcounts);
        
        exposure_ = exposure;
    }
    
    public void setCellNum(int number){
        cellNum = number;
    }
    
    public void setSavePath(String path) {
        savePath = path;
    }
    
    @Override
    public void run() {
        try {album.clear();} catch (IOException e) {ReportingUtils.logError(e, "Error from PWSALBUM");}
        if (studio_.acquisitions().isAcquisitionRunning()) {
            studio_.acquisitions().setPause(true);
        }
        try { 
            if (studio_.live().getIsLiveModeOn()) { //Not supported
                studio_.live().setLiveMode(false);
            }
            /*
            while (Files.isDirectory(Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)))){ //Find a cell number that doesn't already exist.
                cellNum++;
            }
            */
            if (Files.isDirectory(Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)))){
                ReportingUtils.showError("Cell " + cellNum + " already exists");
                return;
            }
            ImSaverRaw imsaver = new ImSaverRaw(studio_, Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)).toString(), imageQueue, metadata, wv, true);
            imsaver.start();
            acquireImages();
        } catch (Exception ex) {          
            ReportingUtils.logError("PWSPlugin, in processor: " + ex.toString());
            imageQueue.clear();
        } finally {
            if (studio_.acquisitions().isAcquisitionRunning()) {
                studio_.acquisitions().setPause(false);
            }
        }
    }
      
    private void acquireImages() {
        try {
            String cam = studio_.core().getCameraDevice();
            studio_.core().waitForDevice(cam);
            studio_.core().clearCircularBuffer();     
            studio_.core().setExposure(cam, exposure_);
            pipeline_ = studio_.data().copyApplicationPipeline(studio_.data().createRAMDatastore(), true);
            
            long now = System.currentTimeMillis();
            
            if (hardwareSequence) {
                String origCameraTrigger="";  
                if (studio_.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")){
                    origCameraTrigger = studio_.core().getProperty(cam, "TRIGGER SOURCE");
                }
                try {
                    studio_.core().startPropertySequence(filtLabel, filtProp);
                    double delayMs = studio_.core().getDeviceDelayMs(filtLabel); //Use the delay defined by the device adapter.
                    if (useExternalTrigger) {
                        if (studio_.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")) { //This device adapter doesn't seem to support delays in the sequence acquisition. We instead set the master pulse interval. We have to assume that the camera is set to trigger from the master pulse. 
                            studio_.core().setProperty(cam, "TRIGGER SOURCE", "EXTERNAL");
                            studio_.core().setProperty(cam, "TRIGGER_DELAY", delayMs/1000); //This is in units of seconds.
                            studio_.core().startSequenceAcquisition(wv.length, 0, false); //The hamamatsu adapter throws an eror if the interval is not 0.
                            int currWv = Integer.parseInt(studio_.core().getProperty(filtLabel, filtProp));
                            studio_.core().setProperty(filtLabel, filtProp, currWv+1); //Trigger a pulse which sets the whole thing off.
                        }   
                    }
                    else { //Since we're not using an external trigger we need to have the camera control the timing.
                        if (studio_.core().getDeviceName(cam).equals("HamamatsuHam_DCAM")) { //This device adapter doesn't seem to support delays in the sequence acquisition. We instead set the master pulse interval.
                            studio_.core().setProperty(cam, "TRIGGER SOURCE", "MASTER PULSE"); //Make sure that MAster Pulse is triggering the camera.
                            double exposurems = studio_.core().getExposure();
                            double readoutms = 10; //This is based on the frame rate calculation portion of the 13440-20CU camera. 9.7 us per line, reading two lines at once, 20148 lines -> 0.097*2048/2 ~= 10
                            studio_.core().setProperty(cam, "MASTER PULSE INTERVAL", (exposurems+readoutms+delayMs)/1000.0);
                            studio_.core().startSequenceAcquisition(wv.length, 0, false); //The hamamatsu adapter throws an eror if the interval is not 0.
                        } else{
                            studio_.core().startSequenceAcquisition(wv.length, delayMs, false);
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
                            addImage(im, i);
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
                }
            }
            else {  //Software sequenced acquisition
                for (int i=0; i<wv.length; i++) {
                    studio_.core().setProperty(filtLabel, filtProp, wv[i]);
                    while (studio_.core().deviceBusy(filtLabel)) {Thread.sleep(1);} //Wait until the device says it is tuned.
                    studio_.core().snapImage();
                    Image im = studio_.data().convertTaggedImage(studio_.core().getTaggedImage());
                    addImage(im, i);
                }
                studio_.core().setProperty(filtLabel, filtProp, wv[0]);
            }
            long itTook = System.currentTimeMillis() - now;         
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("PWS Acquisition took: " + itTook + " milliseconds for "+wv.length + " frames");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logMessage("ERROR: PWSPlugin: " + ex.getMessage());
        } finally {
            try {
                studio_.core().stopPropertySequence(filtLabel, filtProp); //Got to make sure to stop the sequencing behaviour.
            } catch (Exception ex) {
                ex.printStackTrace();
                ReportingUtils.logMessage("ERROR: PWSPlugin: Stopping property sequence: " + ex.getMessage());
            }
        }
    }
    
    private void addImage(Image im, int idx) throws IOException, PipelineErrorException{
        Coords newCoords = im.getCoords().copyBuilder().t(idx).build();
        im = im.copyAtCoords(newCoords);
        pipeline_.insertImage(im);
        im = pipeline_.getDatastore().getImage(newCoords);
        album.addImage(im);                   
        imageQueue.add(im);
    }
}