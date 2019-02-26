/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import java.nio.file.Paths;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import org.micromanager.PropertyMap;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.data.Metadata;
import org.micromanager.data.internal.DefaultMetadata;
import org.micromanager.PropertyMaps;
import org.micromanager.data.Datastore;
import org.micromanager.data.Coords;
import org.micromanager.data.internal.DefaultImageJConverter;
import ij.ImageStack;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.FileInfo;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author N2-LiveCell
 */
public class ImSaverRaw implements Runnable {
    boolean debug_;
    Metadata md_;
    LinkedBlockingQueue queue_;
    Thread t;
    Studio studio_;
    int expectedFrames_;
    int[] wv_;
    String savePath_;

    ImSaverRaw(Studio studio, String savePath, LinkedBlockingQueue queue, Metadata metadata, int[] wavelengths, boolean debug){
        debug_ = debug;
        md_ = metadata;
        queue_ = queue;
        studio_ = studio;
        expectedFrames_ = wavelengths.length;
        savePath_ = savePath;
        wv_ = wavelengths;
        t = new Thread(this, "PWS ImSaver");
        t.start();
    }
    
    @Override
    public void run(){
        try {
            long now = System.currentTimeMillis();
            if (debug_) {
                ReportingUtils.logMessage("PWSPlugin: saving...");
            }
     
            new File(savePath_).mkdirs();
            
            JSONObject jobj = new JSONObject();
            JSONObject md = new JSONObject(((DefaultMetadata)md_).toPropertyMap().toJSON());
            jobj.put("MicroManagerMetadata", md);
            JSONArray WV = new JSONArray();
            for (int i = 0; i < wv_.length; i++) {
                WV.put(wv_[i]);
            }
            jobj.put("wavelengths", WV);  
            jobj.put("exposure", studio_.core().getExposure());
            jobj.put("system", "lcpws2");
            
            FileWriter file = new FileWriter(Paths.get(savePath_).resolve("pwsmetadata.json").toString());
            file.write(jobj.toString());
            file.flush();
            file.close();
            DefaultImageJConverter imJConv = new DefaultImageJConverter();
            ImageStack stack;
            Image im;
                        
            while (queue_.size()<1) { Thread.sleep(10);} //Wait for an image
            im = (Image) queue_.take(); //Lets make an array with the queued images.
            stack = new ImageStack(im.getWidth(), im.getHeight());
            stack.addSlice(imJConv.createProcessor(im));
            PWSAlbum album = new PWSAlbum(studio_);
            album.addImage(im, wv_[0]);
            for (int i=1; i<expectedFrames_; i++) {
                while (queue_.size()<1) { Thread.sleep(10);} //Wait for an image
                im = (Image) queue_.take(); //Lets make an array with the queued images.
                stack.addSlice(imJConv.createProcessor(im));
                album.addImage(im, wv_[i]);
            }
            ImagePlus imPlus = new ImagePlus("PWS", stack);
            imPlus.setProperty("Info", jobj.toString());
            FileInfo info = new FileInfo();
            imPlus.setFileInfo(info);
            FileSaver saver = new FileSaver(imPlus);
            saver.saveAsTiffStack(Paths.get(savePath_).resolve("pws.tif").toString());

            long itTook = System.currentTimeMillis() - now;
            if (debug_) {
                ReportingUtils.logMessage("PWSPlugin: produced image. Saving took:" + itTook + "milliseconds.");
            }
        } catch (Exception ex) {
            ReportingUtils.showError(ex);
            ReportingUtils.logError("Error: PWSPlugin, while producing averaged img: "+ ex.toString());
        } 
        

        
    }
}
