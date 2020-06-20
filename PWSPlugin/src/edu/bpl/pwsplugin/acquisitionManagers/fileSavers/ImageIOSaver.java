/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import com.google.gson.JsonObject;
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
import edu.bpl.pwsplugin.utils.GsonUtils;
import io.scif.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import io.scif.media.imageio.plugins.tiff.TIFFDirectory;
import io.scif.media.imageio.plugins.tiff.TIFFField;
import io.scif.media.imageio.plugins.tiff.TIFFTag;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import mmcorej.org.json.JSONException;

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
        String fullFileName = String.format("%s.tif", this.fileName);
        try (ImageOutputStream outStream = ImageIO.createImageOutputStream(Paths.get(this.savePath, fullFileName).toFile())) {
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
                    
                    if (i == this.expectedFrames/2) { //Note: due to integer division this still works on i==0 for expectedFrames==1.
                        saveThumbnail(image); //Save the thumbnail image from halfway through the sequence.
                    }
                }
                //Now that we've recieved all the images check for the metadata and save it.
                MetadataBase md = this.getMetadataQueue().poll(5, TimeUnit.SECONDS);
                if (md == null) {
                    throw new TimeoutException("ImageIOSaver timed out on receiving metadata.");
                }
                JsonObject mdJson = md.toJson();
                writeMetadata(this.savePath, this.fileName, mdJson); // saves metadata to a text file.
                IIOMetadata iomd = writer.getDefaultStreamMetadata(param);
                //IIOMetadataNode node = new IIOMetadataNode("PWSPluginMetadata");
                //node.setNodeValue(GsonUtils.getGson().toJson(mdJson));
                //iomd.mergeTree(iomd.getNativeMetadataFormatName(), node);
                TIFFDirectory dir = TIFFDirectory.createFromMetadata(iomd);
                TIFFTag tag = BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION);
                TIFFField field = new TIFFField(tag, TIFFTag.TIFF_ASCII, 1, GsonUtils.getGson().toJson(mdJson));
                dir.addTIFFField(field);
                writer.replaceStreamMetadata(dir.getAsMetadata());//Also try to save it to the tiff file.
            } finally { // We don't need to catch any exceptions we can just have them get thrown.
                writer.endWriteSequence();
                writer.dispose();
            }
        }
        long itTook = System.currentTimeMillis() - startTime;
        ReportingUtils.logMessage(String.format("PWSPlugin: ImageIO produced %s image. Saving took:" + itTook + "milliseconds.", this.fileName));
        return null;  
    }
    
    private ImageWriteParam configureWriter(ImageWriter writer) {
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionType("ZLib"); //TODO with how this affects things.
        return param;
    }
    
    private IIOImage MM2IIO(Image mmImg) {
        BufferedImage bim = ImageIOHelper.arrtoim(mmImg.getWidth(), mmImg.getHeight(),(short[]) mmImg.getRawPixels());
        return new IIOImage(bim ,null, null); // We can set metadata later, or maybe we don't need to, no need for thumbnails.
    }
    
    private void saveThumbnail(IIOImage img) throws IOException {
        try (ImageOutputStream ostream = ImageIO.createImageOutputStream(Paths.get(this.savePath, "image_bd.tif").toFile())) {
            ImageIO.write(img.getRenderedImage(), "TIFF", ostream);
        } 
    }
    
    private void writeMetadata(String savePath, String filePrefix, JsonObject md) throws IOException {
        try (FileWriter file = new FileWriter(Paths.get(savePath).resolve(filePrefix + "metadata.json").toString())) {
            file.write(GsonUtils.getGson().toJson(md)); //4 spaces of indentation
            file.flush(); //TODO is this needed.
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