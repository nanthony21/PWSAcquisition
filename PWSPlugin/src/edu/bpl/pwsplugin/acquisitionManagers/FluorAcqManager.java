 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.fileSavers.ImSaverRaw;
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
public class FluorAcqManager implements AcquisitionManager{
    private Studio studio_;
    double exposure_; //The camera exposure in milliseconds.
    String filtLabel_; //The name of the spectral filter device
    int wavelength_; //The wavelength to acquire images at
    String flFilterBlock_; // The name of the fluorescence filter changer device.
    String bfFilterBlock_;
    boolean autoFilter_;
    
    public FluorAcqManager(Studio studio){
        studio_ = studio;
    }
    
    public void setFluorescenceSettings(boolean autoFilter, String bfFilter, String flFilter, double exposure, int emissionWV) {
        autoFilter_ = autoFilter;
        flFilterBlock_ = flFilter;
        bfFilterBlock_ = bfFilter;
        exposure_ = exposure;
        wavelength_ = emissionWV;
    }
    
    @Override
    public void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, JSONObject metadata) {
        try {
            if (autoFilter_) {
                studio_.core().setConfig("Filter", flFilterBlock_); //TODO make sure this waits for the device to switch.
            } else {
                ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`."); //TODO make sure this actually blocks.
            }
            studio_.core().setProperty(filtLabel_, "Wavelength", wavelength_);
            studio_.core().setExposure(exposure_);
            studio_.core().clearCircularBuffer();
            studio_.core().snapImage();
            Image img = studio_.data().convertTaggedImage(studio_.core().getTaggedImage());
            Pipeline pipeline = studio_.data().copyApplicationPipeline(studio_.data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image.                 
            ImSaverRaw imSaver = new ImSaverRaw(studio_, this.getSavePath(savePath, cellNum), imagequeue, this.getExpectedFrames(), true, this.getFilePrefix());
            imSaver.start();
            metadata.put("wavelength", wavelength_);
            metadata.put("exposure", studio_.core().getExposure()); //This must happen after we have set our exposure.
            metadata.put("filterBlock", flFilterBlock_);
            imSaver.setMetadata(metadata);
            imSaver.queue.put(img);
            imSaver.join();
        } catch (Exception e) {
            ReportingUtils.showError(e);
        } finally {
            if (autoFilter_) {
                try {
                    studio_.core().setConfig("Filter", bfFilterBlock_); //TODO make sure this waits for the device to switch.
                } catch (Exception e){
                    ReportingUtils.showError(e);
                }
            } else {
                ReportingUtils.showMessage("Return to the PWS filter block and click `OK`."); //TODO make sure this actually blocks.
            }
        }
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        Path path = Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)).resolve("Fluorescence");
        if (Files.isDirectory(path)){
            ReportingUtils.showError("Cell " + cellNum + " fluorescence already exists.");
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
