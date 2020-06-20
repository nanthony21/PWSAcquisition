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
//import com.twelvemonkeys.imageio.metadata.tiff.TIFF

/**
 *
 * @author nick
 */
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ImageIOSaver extends SaverExecutor {
    private String savePath;
    private String fileName;
    private Integer expectedFrames;
    
    @Override
    public void configure(String savePath, String fileNamePrefix, Integer expectedFrames) {
        this.savePath = savePath;
        this.fileName = fileNamePrefix;
        this.expectedFrames = expectedFrames;
        this.configured = true;
    }
    
    @Override
    public Void call() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersBySuffix("tif").next();        
        ImageWriteParam param = configureWriter(writer);
        
        long startTime = System.currentTimeMillis();
        String fileName = String.format("%s.tif", this.fileName);
        try (ImageOutputStream outStream = ImageIO.createImageOutputStream(Paths.get(this.savePath, fileName).toFile())) {
            writer.setOutput(outStream);
            writer.prepareWriteSequence(null); //null means we will use the default streamMetadata.
            try {
                for (int i=0; i<this.expectedFrames; i++) {
                    Image mmImg = this.getImageQueue().poll(5, TimeUnit.SECONDS);
                    if (mmImg == null) { 
                        throw new TimeoutException(String.format("ImageIOSaver timed out on receiving image %d of %d", i+1, this.expectedFrames));
                    }
                    if (mmImg.getBytesPerPixel() != 2) { //TODO get rid of this constraint.
                        ReportingUtils.showError("PWSPlugin does not support images with other than 16 bit bitdepth.");
                    }
                    IIOImage image = this.MM2IIO(mmImg);
                    writer.writeToSequence(image, param);
                }
                //Now that we've recieved all the images check for the metadata and save it.
                MetadataBase md = this.getMetadataQueue().poll(5, TimeUnit.SECONDS);
                if (md == null) {
                    throw new TimeoutException("ImageIOSaver timed out on receiving metadata.");
                }
                
                //TODO save the thumbnail.
            } finally { // We don't need to catch any exceptions we can just have them get thrown.
                writer.endWriteSequence();
                writer.dispose();
            }
        }
        long itTook = System.currentTimeMillis() - startTime;
        ReportingUtils.logMessage("PWSPlugin: produced image. Saving took:" + itTook + "milliseconds.");
        
        
        return null;  
    }
    
    private ImageWriteParam configureWriter(ImageWriter writer) {
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionType("ZLib");
        return param;
    }
    
    private IIOImage MM2IIO(Image mmImg) {
        BufferedImage bim = ImageIOHelper.arrtoim(mmImg.getWidth(), mmImg.getHeight(),(short[]) mmImg.getRawPixels());
        return new IIOImage(bim ,null, null); // We can set metadata later, or maybe we don't need to, no need for thumbnails.
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
            
            
            JSONObject jobj = new JSONObject();
            JSONObject md = new JSONObject(md_.toString());
            jobj.put("MicroManagerMetadata", md);
            JSONArray WV = new JSONArray();
            for (int i = 0; i < wv_.length; i++) {
                WV.put(wv_[i]);
            }
            jobj.put("waveLengths", WV);  
            jobj.put("exposure", Globals.core().getExposure());
            
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

class ImageIOHelper {
    static BufferedImage arrtoim(int width, int height, short[] arr) {
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