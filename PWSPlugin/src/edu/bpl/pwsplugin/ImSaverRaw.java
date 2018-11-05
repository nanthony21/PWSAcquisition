/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import java.util.concurrent.LinkedBlockingQueue;
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
     
            Metadata.Builder b = md_.copyBuilderWithNewUUID();
            PropertyMap.Builder userData = PropertyMaps.builder();
            userData.putString("system", "lcpws2");
            userData.putIntegerList("wavelengths", wv_);
            userData.putDouble("exposure", studio_.core().getExposure());
            b.userData(userData.build());
            Metadata newMeta = b.build();

            Datastore ds = studio_.data().createMultipageTIFFDatastore(savePath_, false, true);
            ds.setName("PWS");
            Image im;
            Coords.Builder coords;
            for (int i=0; i<expectedFrames_; i++) {
                while (queue_.size()<1) { Thread.sleep(10);} //Wait for an image
                im = (Image) queue_.take(); //Lets make an array with the queued images.
                coords = im.getCoords().copyBuilder();
                coords.channel(i);
                ds.putImage(im.copyWith(coords.build(), newMeta));

            }
            ds.freeze();
            ds.close();

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
