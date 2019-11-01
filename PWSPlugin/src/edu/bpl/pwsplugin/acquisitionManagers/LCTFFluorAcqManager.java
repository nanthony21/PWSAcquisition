 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.fileSavers.ImSaverRaw;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author backman05
 */
public class LCTFFluorAcqManager implements AcquisitionManager{
    double exposure_; //The camera exposure in milliseconds.
    String filtLabel_; //The name of the spectral filter device
    int wavelength_; //The wavelength to acquire images at
    String flFilterBlock_; // The name of the fluorescence filter changer device.
    String bfFilterBlock_;
    boolean autoFilter_;
    
    public LCTFFluorAcqManager(){
    }
    
    public void setFluorescenceSettings(boolean autoFilter, String bfFilter, String flFilter, double exposure, int emissionWV, String tunableFilterLabel) {
        autoFilter_ = autoFilter;
        filtLabel_ = tunableFilterLabel;
        flFilterBlock_ = flFilter;
        bfFilterBlock_ = bfFilter;
        exposure_ = exposure;
        wavelength_ = emissionWV;
    }
    
    @Override
    public void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, JSONObject metadata) {
        try {
            String fullSavePath = this.getSavePath(savePath, cellNum); //This also checks if the file already exists, throws error if it does.
            if (autoFilter_) {
                Globals.core().setConfig("Filter", flFilterBlock_);
                Globals.core().waitForConfig("Filter", flFilterBlock_); // Wait for the device to be ready.
            } else {
                ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`."); //TODO make sure this actually blocks.
            }
            Globals.core().setProperty(filtLabel_, "Wavelength", wavelength_);
            Globals.core().setExposure(exposure_);
            Globals.core().clearCircularBuffer();
            Globals.core().snapImage();
            Image img = Globals.mm().data().convertTaggedImage(Globals.core().getTaggedImage());
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            MMSaver imSaver = new MMSaver(fullSavePath, imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver.start();
            metadata.put("wavelength", wavelength_);
            metadata.put("exposure", Globals.core().getExposure()); //This must happen after we have set our exposure.
            metadata.put("filterBlock", flFilterBlock_);
            imSaver.setMetadata(metadata);
            imSaver.queue.put(img);
            imSaver.join();
        } catch (Exception e) {
            ReportingUtils.showError(e);
        } finally {
            if (autoFilter_) {
                try {
                    Globals.core().setConfig("Filter", bfFilterBlock_);
                    Globals.core().waitForConfig("Filter", bfFilterBlock_); // Wait for the device to be ready.
                } catch (Exception e){
                    ReportingUtils.showError(e);
                }
            } else {
                ReportingUtils.showMessage("Return to the PWS filter block and click `OK`.");
            }
        }
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        Path path = Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)).resolve("Fluorescence");
        if (Files.isDirectory(path)){
            throw new FileAlreadyExistsException("Cell " + cellNum + " fluorescence already exists.");
        } 
        return path.toString();
    }
    
    @Override
    public String getFilePrefix() {
        return "fluor";
    }
    
    @Override
    public int getExpectedFrames() {
        return 1;
    }
}
