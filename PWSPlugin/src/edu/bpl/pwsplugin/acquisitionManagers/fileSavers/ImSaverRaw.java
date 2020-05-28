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
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import java.nio.file.Paths;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.data.Metadata;
import org.micromanager.data.internal.DefaultMetadata;
import org.micromanager.data.ImageJConverter;
import ij.ImageStack;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.io.FileSaver;
import ij.io.FileInfo;
import mmcorej.org.json.JSONObject;
import mmcorej.org.json.JSONException;
import java.io.IOException;
import ij.process.ImageConverter;
import ij.plugin.ContrastEnhancer;
import java.util.concurrent.TimeUnit;


public class ImSaverRaw extends DefaultSaverThread {
    //This isn't currently used. SAves to TIFF using imageJ functions.
    boolean debug_;
    Thread t;
    Studio studio_;
    int expectedFrames_;
    String savePath_;
    ImageJConverter imJConv;
    volatile JSONObject metadata_;
    String filePrefix_;

    public ImSaverRaw(Studio studio, String savePath, LinkedBlockingQueue queue_, int expectedFrames, boolean debug, String filePrefix){
        super(queue_);
        debug_ = debug;
        studio_ = studio;
        expectedFrames_ = expectedFrames;
        savePath_ = savePath;
        imJConv = studio.data().getImageJConverter();
        metadata_ = null;
        filePrefix_ = filePrefix;
    }
    
    @Override
    public void setMetadata(JSONObject md) {
        metadata_ = md;
    }
    
    @Override
    public void run(){
        try {
            long now = System.currentTimeMillis();
            if (debug_) {
                ReportingUtils.logMessage("PWSPlugin: saving...");
            }
            new File(savePath_).mkdirs();
            
            ImageStack stack;
            Image im;
                        
            im = (Image) getQueue().poll(5, TimeUnit.SECONDS); //Lets make an array with the queued images.
            if (im == null) {
                ReportingUtils.showError("ImSaver timed out while waiting for image");
                return;
            }
            
            Metadata md = im.getMetadata();
            JSONObject jmd = new JSONObject(((DefaultMetadata)md).toPropertyMap().toJSON());
       
            stack = new ImageStack(im.getWidth(), im.getHeight());
            stack.addSlice(imJConv.createProcessor(im));
            for (int i=1; i<expectedFrames_; i++) {
                im = (Image) getQueue().poll(1, TimeUnit.SECONDS); //Lets make an array with the queued images.
                if (im == null) {
                    ReportingUtils.showError("ImSaver timed out while waiting for image");
                    return;
                }
                if (i == expectedFrames_/2) {
                    saveImBd(im); //Save the image from halfway through the sequence.
                }
                ImageProcessor proc = imJConv.createProcessor(im);
                stack.addSlice(proc);
            }
            ImagePlus imPlus = new ImagePlus(filePrefix_, stack);
            int i = 0;
            while (metadata_ == null) { //Wait for metadata to be set by the acquistion manager.
                Thread.sleep(10);
                i++;
                if (i > 100) {
                    ReportingUtils.showError("ImSaver timed out while waiting for metadata");
                    return;
                }
            }
            metadata_.put("MicroManagerMetadata", jmd.get("map"));
            writeMetadata();
            imPlus.setProperty("Info", metadata_.toString());
            FileInfo info = new FileInfo();
            imPlus.setFileInfo(info);
            FileSaver saver = new FileSaver(imPlus);
            boolean success = saver.saveAsTiffStack(Paths.get(savePath_).resolve(filePrefix_ + ".tif").toString());
            if (!success) { 
                throw new IOException("Failed to save " + filePrefix_ + " image cube tiff");
            }
            long itTook = System.currentTimeMillis() - now;
            if (debug_) {
                ReportingUtils.logMessage("PWSPlugin: produced image. Saving took:" + itTook + "milliseconds.");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            ReportingUtils.showError(ie);
            ReportingUtils.logError("Error: PWSPlugin, while producing " + filePrefix_ + " image: "+ ie.toString());
        } catch (JSONException | IOException ex) {
            ReportingUtils.showError(ex);
            ReportingUtils.logError("Error: PWSPlugin, while producing " + filePrefix_ + " image: "+ ex.toString());
        } 
    }
    
    private void writeMetadata() throws IOException, JSONException {
            FileWriter file = new FileWriter(Paths.get(savePath_).resolve(filePrefix_ + "metadata.json").toString());
            file.write(metadata_.toString(4)); //4 spaces of indentation
            file.flush();
            file.close();
    }
            
    private void saveImBd(Image im) throws IOException{
        ImagePlus imPlus = new ImagePlus(filePrefix_, imJConv.createProcessor(im));
        ContrastEnhancer contrast = new ContrastEnhancer();
        contrast.stretchHistogram(imPlus,0.01); //I think this will saturate 0.01% of the image. or maybe its 1% idk. 
        ImageConverter converter = new ImageConverter(imPlus);
        converter.setDoScaling(true);
        converter.convertToGray8();
        FileInfo info = new FileInfo();
        imPlus.setFileInfo(info);
        FileSaver saver = new FileSaver(imPlus);
        boolean success = saver.saveAsTiff(Paths.get(savePath_).resolve("image_bd.tif").toString());
        if (!success) {
            throw new IOException("Image BD failed to save");
        }
    }
}
