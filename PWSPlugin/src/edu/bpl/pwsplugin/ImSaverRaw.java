/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.Paths;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.data.internal.DefaultMetadata;
import org.json.JSONArray;
import org.json.JSONObject;
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
import com.twelvemonkeys.imageio.metadata.tiff.TIFF;

/**
 *
 * @author N2-LiveCell
 */
public class ImSaverRaw implements Runnable {
    boolean debug_;
    DefaultMetadata md_;
    LinkedBlockingQueue queue_;
    Thread t;
    Studio studio_;
    int expectedFrames_;
    int[] wv_;
    String savePath_;

    ImSaverRaw(Studio studio, String savePath, LinkedBlockingQueue queue, DefaultMetadata metadata, int[] wavelengths, boolean debug){
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
            Image im = (Image) queue_.take();
            Image oldIm = im;
            int width = im.getHeight();
            int height = im.getWidth();
            int imgDepth = im.getBytesPerPixel();
            
            if (imgDepth != 2) {
                ReportingUtils.showError("PWSPlugin does not support images with other than 16 bit bitdepth.");
            }
            
            int dimension = width * height;
            
            ImageWriter writer = ImageIO.getImageWritersBySuffix("tif").next();
            File file = Paths.get(savePath_).resolve("pws.tif").toFile();
            ImageOutputStream ostream = ImageIO.createImageOutputStream(file);
            writer.setOutput(ostream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            IIOMetadata streamMeta = writer.getDefaultStreamMetadata(param); 
            BufferedImage bim = ImageIOHelper.arrtoim(width,height,(short[]) im.getRawPixels());
            
            
            JSONObject jobj = new JSONObject();
            JSONObject md = new JSONObject(md_.toPropertyMap().toJSON());
            jobj.put("MicroManagerMetadata", md);
            JSONArray WV = new JSONArray();
            for (int i = 0; i < wv_.length; i++) {
                WV.put(wv_[i]);
            }
            jobj.put("waveLengths", WV);  
            jobj.put("exposure", studio_.core().getExposure());
            IIOMetadata meta = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(bim.getType()), param);
            IIOMetadataNode tree = (IIOMetadataNode) meta.getAsTree(meta.getMetadataFormatNames()[0]);
            ImageIOHelper.createTIFFFieldNode((IIOMetadataNode) tree.getFirstChild(), TIFF.TAG_IMAGE_DESCRIPTION, TIFF.TYPE_ASCII, jobj.toString());
            meta.setFromTree(meta.getMetadataFormatNames()[0], tree);
                    
            writer.prepareWriteSequence(streamMeta);
            IIOImage newIm = new IIOImage(bim ,null, meta);
            writer.writeToSequence(newIm, param);
            
            for (int i=1; i<expectedFrames_; i++) {
                while (queue_.size()<1) { Thread.sleep(10);} //Wait for an image
                im = (Image) queue_.take(); //Lets make an array with the queued images.
                short[] imArr = (short[]) im.getRawPixels();
                bim = ImageIOHelper.arrtoim(width, height, imArr);
                newIm = new IIOImage(bim ,null, meta);
                writer.writeToSequence(newIm, param);
            }

            writer.endWriteSequence();
            writer.dispose();
            ostream.close();

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
