package edu.bpl.pwsplugin;

import org.micromanager.internal.utils.ReportingUtils;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.StrVector;
import org.micromanager.data.Processor;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorContext;
import org.micromanager.data.Image;
import org.micromanager.data.SummaryMetadata;
import org.micromanager.data.Metadata;
import org.micromanager.data.internal.DefaultMetadata;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;



public class PWSProcessor extends Processor {
    Studio studio_;
    int numAverages_;
    LinkedBlockingQueue imageQueue;
    boolean debugLogEnabled_ = true;
    Image[] imageArray;
    int[] wv;
    String filtLabel;
    String filtProp;
    Boolean hardwareSequence;
    Boolean useExternalTrigger;
    String savePath;
    int cellNum;
    JSONObject metadata = new JSONObject();
    public PWSProcessor(Studio studio, PropertyMap settings) throws Exception{
        studio_ = studio;
        
        wv = settings.getIntegerList(PWSPlugin.wvSetting);
        filtLabel = settings.getString(PWSPlugin.filterLabelSetting, "");
        hardwareSequence = settings.getBoolean(PWSPlugin.sequenceSetting, false);
        savePath = settings.getString(PWSPlugin.savePathSetting, "");
        useExternalTrigger = settings.getBoolean(PWSPlugin.externalTriggerSetting, false);
        cellNum = settings.getInteger(PWSPlugin.cellNumSetting,1);
        int darkCounts = settings.getInteger(PWSPlugin.darkCountsSetting,0);
        int[] linearityPolynomial = settings.getIntegerList(PWSPlugin.linearityPolySetting);
        String systemName = settings.getString(PWSPlugin.systemNameSetting, "");
        
        JSONArray WV = new JSONArray();
        for (int i = 0; i < wv.length; i++) {
            WV.put(wv[i]);
        }
        metadata.put("wavelengths", WV);  
        metadata.put("system", systemName);
        metadata.put("darkCounts", darkCounts);
        metadata.put("linearityPoly", linearityPolynomial);

        filtProp = "Wavelength";
        studio_.acquisitions().attachRunnable(-1, -1, -1, -1, new PWSRunnable(this)); 
        imageQueue = new LinkedBlockingQueue();
        
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
    public void cleanup(ProcessorContext context) {
            studio_.acquisitions().clearRunnables();
    }
    
    @Override
    public SummaryMetadata processSummaryMetadata(SummaryMetadata metadata) {
        SummaryMetadata.Builder builder = metadata.copyBuilder();
        builder.userName("PWSAcquisition");
        return builder.build();
    }
    
    @Override
    public void processImage(Image image, ProcessorContext context) {
        Image imageOnError = image;
        try { 
            if (studio_.live().getIsLiveModeOn()) { //Not supported
                return;
            }
            else {
                Metadata md = image.getMetadata();
                JSONObject jmd = new JSONObject(((DefaultMetadata)md).toPropertyMap().toJSON());
                metadata.put("MicroManagerMetadata", jmd);
                metadata.put("exposure", studio_.core().getExposure());

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
                if (!studio_.acquisitions().isAcquisitionRunning()) { //This means we must be in snap mode. There is no runnable so we must acquire the image here.
                    acquireImages();
                }
                //Nothing extra needs to be done for acquisition mode
                if (debugLogEnabled_) {
                    ReportingUtils.logMessage("Queue has" + Integer.toString(imageQueue.size()));
                }
            }

        } catch (Exception ex) {          
            ReportingUtils.logError("PWSPlugin, in processor: " + ex.toString());
            imageQueue.clear();
        }
        finally {
            context.outputImage(imageOnError);
        }
    }

    
         
    public void acquireImages() {
        try {
            String cam = studio_.core().getCameraDevice();
            studio_.core().waitForDevice(cam);
            studio_.core().clearCircularBuffer();     
            long now = System.currentTimeMillis();
            
            if (hardwareSequence) {
    //          CMMCore::startSequenceAcquisition(long numImages, double intervalMs, bool stopOnOverflow)
    //          @param numImages Number of images requested from the camera
    //          @param intervalMs interval between images, currently only supported by Andor cameras
    //          @param stopOnOverflow whether or not the camera stops acquiring when the circular buffer is full
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
                    while (true) {
                        boolean remaining = (studio_.core().getRemainingImageCount() > 0);
                        boolean running = (studio_.core().isSequenceRunning(cam));
                        if ((!remaining) && (canExit)) {
                            break;  //Everything is taken care of.
                        }
                        if (remaining) {    //Process images
                           imageQueue.add(studio_.data().convertTaggedImage(studio_.core().popNextTaggedImage()));
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
                    imageQueue.add(studio_.data().convertTaggedImage(studio_.core().getTaggedImage()));
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
    
}