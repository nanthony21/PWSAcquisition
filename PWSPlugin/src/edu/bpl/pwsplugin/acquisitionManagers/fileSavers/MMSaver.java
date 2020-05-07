
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import edu.bpl.pwsplugin.Globals;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import mmcorej.org.json.JSONException;
import mmcorej.org.json.JSONObject;
import org.micromanager.data.Image;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.data.Metadata;
import org.micromanager.data.Datastore;
import org.micromanager.data.Coords;
import org.micromanager.data.internal.DefaultMetadata;

public class MMSaver extends SaverThread {
    //A thread that saves a tiff file using Micro-Managers `DataStore`. Metadata is saved to a separate json file.
    public LinkedBlockingQueue queue;
    int expectedFrames_;
    String savePath_;
    JSONObject metadata_;
    String filePrefix_;

    public MMSaver(String savePath, LinkedBlockingQueue imageQueue, int expectedFrames, String filePrefix){
        queue = imageQueue; //The queue to receive images through.
        expectedFrames_ = expectedFrames; // The number of image frames that are expected to be received via queue
        savePath_ = savePath; // The file path to save to
        filePrefix_ = filePrefix; // The prefix to name the image file by. This is used by the analysis software to find images.
    }
    
    @Override
    public void run(){
        try {
            long now = System.currentTimeMillis(); 
            Datastore ds = Globals.mm().data().createMultipageTIFFDatastore(savePath_, false, true);
            ds.setName("PWSPluginSaver");
            Image im;
            Coords.Builder coords;
            JSONObject jmd = null;
            for (int i=0; i<expectedFrames_; i++) {
                im = (Image) queue.poll(5, TimeUnit.SECONDS); //Wait for an image
                if (i==0) {
                    Metadata md = im.getMetadata();
                    jmd = new JSONObject(((DefaultMetadata)md).toPropertyMap().toJSON()); //Save the micromanager metadata from the first image.
                }
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
            ds.freeze(); //This must be called prior to closing or the file will be corrupted.
            ds.close();

            File oldFile = new File(savePath_).listFiles((dir, name) -> name.endsWith(".ome.tif") && name.contains("MMStack"))[0];
            File newFile = new File(Paths.get(savePath_).resolve(filePrefix_ + ".tif").toString());
            oldFile.renameTo(newFile);
            
            //make sure the metadata has been set.
            int i = 0;
            while (metadata_ == null) { //Wait for metadata to be set by the acquistion manager.
                Thread.sleep(10);
                i++;
                if (i > 100) {
                    ReportingUtils.showError("ImSaver timed out while waiting for metadata");
                    return;
                }
            }
            metadata_.put("MicroManagerMetadata", jmd.get("map")); //Add the micromanager metadata.
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
        ImagePlus imPlus = new ImagePlus(filePrefix_, Globals.mm().data().ij().createProcessor(im));
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