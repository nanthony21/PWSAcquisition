/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.Paths;
import org.micromanager.data.Image;
import org.micromanager.internal.utils.ReportingUtils;
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
import com.twelvemonkeys.imageio.metadata.tiff.TIFF

/**
 *
 * @author nick
 */
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.metadata.MetadataBase;

public class ImageIOSaver extends SaverExecutor {
    
    @Override
    public Void call() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersBySuffix("tif").next();
        long now = System.currentTimeMillis();
        return null;  
    }
    
}

public class ImSaverRawp implements Runnable {
    boolean debug_;
    Metadata md_;
    LinkedBlockingQueue queue_;
    Thread t;
    int expectedFrames_;
    int[] wv_;
    String savePath_;
    
    @Override
    public void run(){
        try {
            Image im = (Image) queue_.take();
            int width = im.getHeight();
            int height = im.getWidth();
            
            if (im.getBytesPerPixel() != 2) {
                ReportingUtils.showError("PWSPlugin does not support images with other than 16 bit bitdepth.");
            }
                        
            ImageWriter writer = ImageIO.getImageWritersBySuffix("tif").next();
            File file = Paths.get(savePath_).resolve("pws.comp.tif").toFile();
            ImageOutputStream ostream = ImageIO.createImageOutputStream(file);
            writer.setOutput(ostream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("ZLib");
            IIOMetadata streamMeta = writer.getDefaultStreamMetadata(param); 
            BufferedImage bim = ImageIOHelper.arrtoim(width,height,(short[]) im.getRawPixels());
            IIOMetadata  meta = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(bim.getType()), param);
            
            
            JSONObject jobj = new JSONObject();
            JSONObject md = new JSONObject(md_.toString());
            jobj.put("MicroManagerMetadata", md);
            JSONArray WV = new JSONArray();
            for (int i = 0; i < wv_.length; i++) {
                WV.put(wv_[i]);
            }
            jobj.put("waveLengths", WV);  
            jobj.put("exposure", Globals.core().getExposure());
            meta = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(bim.getType()), param);
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
