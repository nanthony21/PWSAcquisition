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

import org.micromanager.data.Processor;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorContext;
import org.micromanager.data.Image;
import org.micromanager.data.SummaryMetadata;
import org.micromanager.acquisition.internal.TaggedImageQueue;
import org.micromanager.data.Metadata;
import javax.imageio.ImageWriter;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.ImageTypeSpecifier;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter; 
import com.twelvemonkeys.imageio.metadata.tiff.TIFF;



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
            savePWS(imageArray, md);
            context.outputImage(imageArray[imageArray.length/2]);   //Return the middle image.
        } catch (Exception ex) {
            context.outputImage(imageOnError);            
            ReportingUtils.logError("PWSPlugin, in Process: " + ex.toString());
            imageQueue.clear();
        }
    }

    private void savePWS(Image[] imArray, Metadata metadata) {
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
            int[] sub = new int[dimension];
            int[] min = new int[imArray.length-1];
            Object[] subsarray = new Object[imArray.length];
            subsarray[0] = (int[]) imArray[0].getRawPixels();
            for (int i = 1; i < imArray.length; i++) {
                int[] old = (int[]) imArray[i-1].getRawPixels();
                int[] New = (int[]) imArray[i].getRawPixels();
                min[i-1] = 32767;
                for (int j = 0; j < dimension; j++) {
                    sub[j] =  ((int) New[j] - (int) old[j]);
                    if (sub[j] < min[i-1]) {
                        min[i-1] = sub[j];
                    }
                }
                int[] ssub = new int[dimension];
                for (int j = 0; j < dimension; j++) {
                    ssub[j] = (int) (sub[j] - min[i-1]);
                }
                subsarray[i] = ssub;              
            }
            JSONObject jobj = new JSONObject();
            JSONObject md = new JSONObject(metadata.toString());
            jobj.put("MicroManagerMetadata", md);
            JSONArray WV = new JSONArray();
            for (int i = 0; i < wv.length; i++) {
                WV.put(wv[i]);
            }
            JSONArray Min = new JSONArray();
            for (int i = 0; i < min.length; i++) {
                Min.put(min[i]);
            } 
            jobj.put("waveLengths", WV);  
            jobj.put("exposure", studio_.core().getExposure());
            jobj.put("compressionMins", Min);
            FileWriter filew = new FileWriter("E:\\Nick\\md.txt");
            filew.write(jobj.toString());
            filew.close();
            ImageWriter writer = ImageIO.getImageWritersBySuffix("tif").next();
            File file = new File("E:\\Nick\\Tiffy3.tif");
            ImageOutputStream ostream = ImageIO.createImageOutputStream(file);
            writer.setOutput(ostream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("ZLib");
            IIOMetadata streamMeta = writer.getDefaultStreamMetadata(param);     
            BufferedImage bim = arrtoim(width,height,(int[])imArray[0].getRawPixels());
            IIOMetadata  meta = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(bim.getType()), param);
            IIOMetadataNode tree = (IIOMetadataNode) meta.getAsTree(meta.getMetadataFormatNames()[0]);
            createTIFFFieldNode((IIOMetadataNode) tree.getFirstChild(), TIFF.TAG_IMAGE_DESCRIPTION, TIFF.TYPE_ASCII, jobj.toString());
            meta.setFromTree(meta.getMetadataFormatNames()[0], tree);
            writer.prepareWriteSequence(streamMeta);
            for (int i = 0; i < subsarray.length; i++) {
                bim = arrtoim(width,height,(int[])subsarray[i]);
                IIOImage im = new IIOImage(bim ,null, meta);
                writer.writeToSequence(im, param);
            }
            writer.endWriteSequence();
            writer.dispose();
            ostream.close();

            long itTook = System.currentTimeMillis() - now;
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("PWSPlugin: produced image. Saving took:" + itTook + "milliseconds.");
            }
        } catch (Exception ex) {
            ReportingUtils.showError(ex);
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
    
    static BufferedImage arrtoim(int width, int height, int[] arr) {
        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster r = (WritableRaster) im.getData();
        int[] newarr = new int[arr.length];
        for (int i=0; i<newarr.length; i++) {
            newarr[i] = (int) arr[i];
        }
        r.setPixels(0,0,width,height,newarr);
        im.setData(r);
        return im;
    }
    
    static void createTIFFFieldNode(final IIOMetadataNode parentIFDNode, int tag, short type, Object value) {
        IIOMetadataNode fieldNode = new IIOMetadataNode("TIFFField");

        fieldNode.setAttribute("number", String.valueOf(tag));
        parentIFDNode.appendChild(fieldNode);

        switch (type) {
            case TIFF.TYPE_ASCII:
                createTIFFFieldContainerNode(fieldNode, "Ascii", value);
                break;
            case TIFF.TYPE_BYTE:
                createTIFFFieldContainerNode(fieldNode, "Byte", value);
                break;
            case TIFF.TYPE_SHORT:
                createTIFFFieldContainerNode(fieldNode, "Short", value);
                break;
            case TIFF.TYPE_RATIONAL:
                createTIFFFieldContainerNode(fieldNode, "Rational", value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    static void createTIFFFieldContainerNode(final IIOMetadataNode fieldNode, final String type, final Object value) {
        IIOMetadataNode containerNode = new IIOMetadataNode("TIFF" + type + "s");
        fieldNode.appendChild(containerNode);

        IIOMetadataNode valueNode = new IIOMetadataNode("TIFF" + type);
        valueNode.setAttribute("value", String.valueOf(value));
        containerNode.appendChild(valueNode);
    }
}