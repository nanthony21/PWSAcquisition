/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.Globals;
import java.nio.file.Paths;
import org.micromanager.data.Image;
import org.micromanager.internal.utils.ReportingUtils;
import javax.imageio.ImageWriter;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.imageio.stream.ImageOutputStream;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageConverter;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.NoSuchElementException;


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
    public void runInThread() throws Exception { //This was tested using the TwelveMonkeys imageIO plugin for TIFF. In theory it should work for any ImageIO tiff plugin.
        ImageWriter writer;
        try {
            writer = ImageIO.getImageWritersBySuffix("tif").next();
        } catch (NoSuchElementException e) { //This will throw an error if a Tiff plugin can't be found. The default java version for Micro-Manager is version 8 which doesn't have  a plugin by default. One solution is just to make sure to use java 9 or higher.   
            throw new NoSuchElementException("An ImageIO plugin for saving TIFF files could not be found. Please install the TwelveMonkeys TIFF plugin.");
        }
        ImageWriteParam param = configureWriter(writer);
        
        long startTime = System.currentTimeMillis();
        String fullFileName = String.format("%s.tif", this.fileName);
        File directory = new File(this.savePath);
        if (!directory.exists()) { directory.mkdirs(); }
        try (FileOutputStream innerStream = new FileOutputStream(Paths.get(this.savePath, fullFileName).toFile()); 
                ImageOutputStream outStream = ImageIO.createImageOutputStream(innerStream)) {
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
                        saveThumbnail(mmImg, this.savePath, this.fileName); //Save the thumbnail image from halfway through the sequence.
                    }
                }
                //Now that we've recieved all the images check for the metadata and save it.
                MetadataBase md = this.getMetadataQueue().poll(5, TimeUnit.SECONDS);
                if (md == null) {
                    throw new TimeoutException("ImageIOSaver timed out on receiving metadata.");
                }
                writeMetadata(this.savePath, this.fileName, md.toJson()); // saves metadata to a text file.
                //Twelve monkeys does not support writing metadata after the fact, just use the json text file.
            } finally { // We don't need to catch any exceptions we can just have them get thrown.
                writer.endWriteSequence();
                writer.dispose();
            }
        }
        long itTook = System.currentTimeMillis() - startTime;
        ReportingUtils.logMessage(String.format("PWSPlugin: ImageIO produced %s image(s). Saving took: %d milliseconds.", this.fileName, itTook));
    }
    
    private ImageWriteParam configureWriter(ImageWriter writer) {
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionType("None");//("ZLib"); //I had hoped that this would speed up saving but it didn't. compression only helps a by ~10% of disk space
        return param;
    }
    
    private IIOImage MM2IIO(Image mmImg) {
        //This implementation will only work for 2byte pixels.
        BufferedImage bim = ImageIOHelper.arrtoim(mmImg.getWidth(), mmImg.getHeight(),(short[]) mmImg.getRawPixels());
        return new IIOImage(bim ,null, null); // We can set metadata later, or maybe we don't need to, no need for thumbnails.
    }
    
    private void saveThumbnail(Image im, String savePath, String filePrefix) throws IOException{
        ImagePlus imPlus = new ImagePlus(filePrefix, Globals.mm().data().ij().createProcessor(im));
        ContrastEnhancer contrast = new ContrastEnhancer();
        contrast.stretchHistogram(imPlus,0.01); //I think this will saturate 0.01% of the image. or maybe its 1% idk. 
        ImageConverter converter = new ImageConverter(imPlus);
        converter.setDoScaling(true);
        converter.convertToGray8();
        FileInfo info = new FileInfo();
        imPlus.setFileInfo(info);
        FileSaver saver = new FileSaver(imPlus);
        boolean success = saver.saveAsTiff(Paths.get(savePath).resolve("image_bd.tif").toString());
        if (!success) {
            throw new IOException("Image BD failed to save");
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
}