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
import mmcorej.StrVector;
import ij.ImagePlus;
import ij.ImageStack;
import org.micromanager.data.Processor;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorContext;
import org.micromanager.data.Image;
import org.micromanager.data.SummaryMetadata;
import org.micromanager.acquisition.internal.TaggedImageQueue;
import org.micromanager.data.Coords.CoordsBuilder;
import org.micromanager.data.Coords;
import org.micromanager.data.Metadata;
import org.micromanager.data.internal.DefaultImage;
import net.sf.ij.jaiio.JAIWriter;
import non_com.media.jai.codec.TIFFEncodeParam;


public class PWSProcessor extends Processor {
 
    Studio studio_;
    int numAverages_;
    TaggedImageQueue imageQueue;
    boolean debugLogEnabled_ = true;
    Image[] imageArray;
    Integer[] wv;
    String filtLabel;
    String filtProp;
    
    public PWSProcessor(Studio studio, PropertyMap settings){
        studio_ = studio;
        wv = settings.getIntArray("wv");
        filtLabel = settings.getString("filtLabel");
        filtProp = "Wavelength";
        imageArray = new Image[wv.length];
        studio_.acquisitions().attachRunnable(-1, -1, -1, -1, new PWSRunnable(this)); 
        imageQueue = new TaggedImageQueue();
        
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
            while (!imageQueue.isEmpty()) {
                imageArray[i++] = studio_.data().convertTaggedImage(imageQueue.take()); //Lets make an array with the queued images.
            }
            Metadata md = image.getMetadata();
            Coords co = image.getCoords();
            savePWS(imageArray, co, md);
            context.outputImage(imageArray[imageArray.length/2]);   //Return the middle image.
        } catch (Exception ex) {
            context.outputImage(imageOnError);            
            ReportingUtils.logError("PWSPlugin, in Process: " + ex.toString());
            imageQueue.clear();
        }
    }

    private void savePWS(Image[] imArray, Coords coords, Metadata metadata) {
        try {
            long now = System.currentTimeMillis();
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("PWSPlugin: computing...");
            }
            int width = imArray[0].getHeight();
            int height = imArray[0].getWidth();
            int imgDepth = imArray[0].getBytesPerPixel();
            
            if (imgDepth != 2) {
                studio_.logs().showError("PWSPlugin does not support images with other than 16 bit bitdepth.");
            }

            int dimension = width * height;
            short[] old;   
            int[] sub = new int[dimension];
            Object result = null;
            int[] min = new int[imArray.length-1];
            JAIWriter writer = new JAIWriter();
            writer.setFormatName("TIFF");
            TIFFEncodeParam param = new TIFFEncodeParam();
            param.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
            param.setDeflateLevel(1);
            writer.setImageEncodeParam(param);
            ImageStack stack = new ImageStack(width,height);
            
            CoordsBuilder co = coords.copy();
            co.channel(0);
            Image firstIm = imArray[0].copyWith(co.build(), metadata);
            stack.addSlice(studio_.data().ij().createProcessor(firstIm));
            for (int i = 1; i < imArray.length; i++) {
                old = (short[]) imArray[i-1].getRawPixels();
                short[] New = (short[]) imArray[i].getRawPixels();
                min[i-1] = 32767;
                for (int j = 0; j < dimension; j++) {
                    sub[j] =  ((int) New[j] - (int) old[j]);
                    if (sub[j] < min[i-1]) {
                        min[i-1] = sub[j];
                    }
                }
                short[] ssub = new short[dimension];
                for (int j = 0; j < dimension; j++) {
                    ssub[j] = (short) (sub[j] - min[i-1]);
                }
                co.channel(i);
                result = ssub;
                stack.addSlice(studio_.data().ij().createProcessor(new DefaultImage(result,width,height,imgDepth,1,co.build(),metadata)));           
            }
            ImagePlus im = new ImagePlus("PWSacq", stack);
            writer.write("E:\\Nick\\Tiffy.tif",im);
            long itTook = System.currentTimeMillis() - now;
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("PWSPlugin: produced image. Saving took:" + itTook + "milliseconds.");
            }
        } catch (Exception ex) {
            ReportingUtils.logError("Error: PWSPlugin, while producing averaged img: "+ ex.toString());
        }
    }
         
    public void acquireImages() {
        try {
            studio_.core().waitForDevice(studio_.core().getCameraDevice());
            studio_.core().clearCircularBuffer();
            String cam = studio_.core().getCameraDevice();
            
//          CMMCore::startSequenceAcquisition(long numImages, double intervalMs, bool stopOnOverflow)
//          @param numImages Number of images requested from the camera
//          @param intervalMs interval between images, currently only supported by Andor cameras
//          @param stopOnOverflow whether or not the camera stops acquiring when the circular buffer is full
            
            studio_.core().startPropertySequence(filtLabel, filtProp);
                            
            studio_.core().startSequenceAcquisition(wv.length, 0, false);
            
            long now = System.currentTimeMillis();
            int frame = 1;// keep 0 free for the image from engine
            // reference BurstExample.bsh
            
            boolean canExit = false;
            while (true) {
                boolean remaining = (studio_.core().getRemainingImageCount() > 0);
                boolean running = (studio_.core().isSequenceRunning(cam));
                if ((!remaining) && (canExit)) {
                    break;  //Everything is taken care of.
                }
                if (remaining) {    //Process images
                   imageQueue.add(studio_.core().popNextTaggedImage());
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
            long itTook = System.currentTimeMillis() - now;
            try {
                studio_.core().stopSequenceAcquisition();  
            } catch (Exception ex) {
                ex.printStackTrace();
                ReportingUtils.logMessage("ERROR: PWSPlugin: " + ex.getMessage());
            }          
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("PWS Acquisition took: " + itTook + " milliseconds for "+wv.length + " frames");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logMessage("PWSPlugin Error");
        }
    }
}