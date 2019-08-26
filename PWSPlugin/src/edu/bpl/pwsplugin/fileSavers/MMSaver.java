/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.fileSavers;

import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageConverter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import org.micromanager.PropertyMap;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.data.Metadata;
import org.micromanager.PropertyMaps;
import org.micromanager.data.Datastore;
import org.micromanager.data.Coords;

/**
 *
 * @author N2-LiveCell
 */
public class MMSaver extends SaverThread {
    LinkedBlockingQueue queue_;
    Studio studio_;
    int expectedFrames_;
    String savePath_;
    JSONObject metadata_;
    String filePrefix_;

    MMSaver(Studio studio, String savePath, LinkedBlockingQueue queue, String filePrefix, int expectedFrames){
        queue_ = queue;
        studio_ = studio;
        expectedFrames_ = expectedFrames;
        savePath_ = savePath;
        filePrefix_ = filePrefix;
    }
    
    @Override
    public void run(){
        try {
            long now = System.currentTimeMillis(); 
            Datastore ds = studio_.data().createMultipageTIFFDatastore(savePath_, false, true);
            ds.setName("PWS");
            Image im;
            Coords.Builder coords;
            for (int i=0; i<expectedFrames_; i++) {
                im = (Image) queue_.poll(5, TimeUnit.SECONDS); //Wait for an image
                if (im == null) {
                    ReportingUtils.showError("ImSaver timed out while waiting for image");
                    return;
                }
                coords = im.getCoords().copyBuilder();
                coords.timePoint(i);
                ds.putImage(im.copyAtCoords(coords.build()));
                
                if (i == expectedFrames_/2) {
                    saveImBd(im); //Save the image from halfway through the sequence.
                }
            }
            ds.freeze();
            ds.close();

      
            //make sure it's been set.
            int i = 0;
            while (metadata_ == null) { //Wait for metadata to be set by the acquistion manager.
                Thread.sleep(10);
                i++;
                if (i > 100) {
                    ReportingUtils.showError("ImSaver timed out while waiting for metadata");
                    return;
                }
            }
            writeMetadata();
            
            long itTook = System.currentTimeMillis() - now;
            ReportingUtils.logMessage("PWSPlugin: produced image. Saving took:" + itTook + "milliseconds.");
            
        } catch (Exception ex) {
            ReportingUtils.showError(ex);
            ReportingUtils.logError("Error: PWSPlugin, while producing averaged img: "+ ex.toString());
        } 
    }
        
    @Override
    public void setMetadata(JSONObject md) {
        metadata_ = md;
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
