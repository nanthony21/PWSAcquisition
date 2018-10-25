package edu.bpl.pwsplugin;

/*
 * Copyright © 2009 – 2013, Marine Biological Laboratory
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of 
 * the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of any organization.
 * 
 * Multiple-Frame Averaging plug-in for Micro-Manager
 * @author Amitabh Verma (averma@mbl.edu), Grant Harris (gharris@mbl.edu)
 * Marine Biological Laboratory, Woods Hole, Mass.
 * 
 */

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




public class PWSProcessor extends Processor {
 
    Studio studio_;
    int numAverages_;
    LinkedBlockingQueue imageQueue;
    boolean debugLogEnabled_ = true;
    Image[] imageArray;
    Integer[] wv;
    String filtLabel;
    String filtProp;
    Boolean hardwareSequence;
    
    public PWSProcessor(Studio studio, PropertyMap settings){
        studio_ = studio;
        wv = settings.getIntArray("wv");
        filtLabel = settings.getString("filtLabel");
        hardwareSequence = settings.getBoolean("sequence");
        filtProp = "Wavelength";
        imageArray = new Image[wv.length];
        studio_.acquisitions().attachRunnable(-1, -1, -1, -1, new PWSRunnable(this)); 
        imageQueue = new LinkedBlockingQueue();
        
        if (hardwareSequence) {
            try {
                if (!studio_.core().isPropertySequenceable(filtLabel, filtProp)){
                    ReportingUtils.showError("The filter device does not have a sequenceable 'Wavelength' property.");
                }
                if (studio_.core().getPropertySequenceMaxLength(filtLabel, filtProp) < wv.length) {
                    ReportingUtils.showError("The filter device does not support sequencing as many wavelenghts as have been specified. Max is " + Integer.toString(studio_.core().getPropertySequenceMaxLength(filtLabel, filtProp)));
                }
                StrVector strv = new StrVector();
                for (int i = 0; i < wv.length; i++) {   //Convert wv from int to string for sending to the device.
                    strv.add(String.valueOf(wv[i]));
                }
                studio_.core().loadPropertySequence(filtLabel, filtProp, strv);
            }
            catch (Exception ex) {
                ReportingUtils.showError(ex);
            }
        }             
    }
    
    @Override
    public void cleanup(ProcessorContext context) {
            studio_.acquisitions().clearRunnables();
    }
    
    @Override
    public SummaryMetadata processSummaryMetadata(SummaryMetadata metadata) {
        SummaryMetadata.SummaryMetadataBuilder builder = metadata.copy();
        builder.userName("PWSAcquisition");
        return builder.build();
    }
    
    @Override
    public void processImage(Image image, ProcessorContext context) {
        Image imageOnError = image;
        try { 
            if (studio_.live().getIsLiveModeOn()) { //Not supported
                context.outputImage(imageOnError); 
                return;
            }
            else if (!studio_.acquisitions().isAcquisitionRunning()) { //This means we must be in snap mode. There is no runnable so we must acquire the image here.
                acquireImages();
            }
            //Nothing extra needs to be done for acquisition mode
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("Queue has" + Integer.toString(imageQueue.size()));
            }
            
            int i = 0;  //The original image is just going to be thrown out :( .

            Metadata md = image.getMetadata();
            ImSaver imsaver = new ImSaver(studio_, imageQueue, md, wv, true);
            context.outputImage(imageArray[imageArray.length/2]);   //Return the middle image.
        } catch (Exception ex) {
            context.outputImage(imageOnError);            
            ReportingUtils.logError("PWSPlugin, in Process: " + ex.toString());
            imageQueue.clear();
        }
    }

    
         
    public void acquireImages() {
        try {
            studio_.core().waitForDevice(studio_.core().getCameraDevice());
            studio_.core().clearCircularBuffer();
            String cam = studio_.core().getCameraDevice();
            long now = System.currentTimeMillis();
            
            if (hardwareSequence) {
    //          CMMCore::startSequenceAcquisition(long numImages, double intervalMs, bool stopOnOverflow)
    //          @param numImages Number of images requested from the camera
    //          @param intervalMs interval between images, currently only supported by Andor cameras
    //          @param stopOnOverflow whether or not the camera stops acquiring when the circular buffer is full
                studio_.core().startPropertySequence(filtLabel, filtProp);

                studio_.core().startSequenceAcquisition(wv.length, 0, false);

                int frame = 1;// keep 0 free for the image from engine
                // reference BurstExample.bsh

                boolean canExit = false;
                while (true) {
                    boolean remaining = (studio_.core().getRemainingImageCount() > 0);
                    boolean running = (studio_.core().isSequenceRunning(cam));
                    if ((!remaining) && (canExit)) {
                        studio_.core().stopSequenceAcquisition(); 
                        break;  //Everything is taken care of.
                    }
                    if (remaining) {    //Process images
                       imageQueue.add(studio_.data().convertTaggedImage(studio_.core().popNextTaggedImage()));
                       frame++;
                       /*
                        if (proc_.display_ != null) {
                            if (proc_.display_.acquisitionIsRunning()) {
                                proc_.display_.displayStatusLine("Image Avg. Acquiring No. " + frame);
                            }
                        }
                        */
                    }
                    if (!running) {
                        studio_.core().stopPropertySequence(filtLabel, filtProp);
                        canExit = true;
                    }
                 }
            }
            else {  //Software sequenced acquisition
                for (int i=0; i<wv.length; i++) {
                    studio_.core().setProperty(filtLabel, filtProp, wv[i]);
                    while (studio_.core().deviceBusy(filtLabel)) {Thread.sleep(1);} //Wait until the device says it is tuned.
                    imageQueue.add(studio_.live().snap(true));
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
        }
    }
    
}