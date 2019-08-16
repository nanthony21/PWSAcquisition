///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2019
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.ImSaverRaw;
import edu.bpl.pwsplugin.PWSAlbum;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;


public class DynAcqManager implements AcquisitionManager{
    private Studio studio_;
    double exposure_; //The camera exposure in milliseconds.
    String filtLabel_; 
    int wavelength_; //The wavelength to acquire images at
    int numFrames_; // The number of images to acquire.
    PWSAlbum album_;
    
    public DynAcqManager(Studio studio, PWSAlbum album){
        studio_ = studio;
        album_ = album;
    }
    
    public void setSequenceSettings(double exposure, String filtLabel, int wavelength, int numFrames) {
        exposure_ = exposure;
        filtLabel_ = filtLabel;
        wavelength_ = wavelength;
        numFrames_ = numFrames;
    }
    
    @Override
    public void acquireImages(ImSaverRaw imSaver, JSONObject metadata) {
        try {album_.clear();} catch (IOException e) {ReportingUtils.logError(e, "Error from PWSALBUM");}
        try {
            studio_.core().setProperty(filtLabel_, "Wavelength", wavelength_);
            studio_.core().setExposure(exposure_);
            studio_.core().setCircularBufferMemoryFootprint(1000); //increase the circular buffer to 1Gb to avoid weird issues with lost images
            studio_.core().clearCircularBuffer();
            studio_.core().startSequenceAcquisition(numFrames_, 0, false);
        } catch (Exception e) {
            ReportingUtils.showError(e);
        }
        
        Pipeline pipeline = studio_.data().copyApplicationPipeline(studio_.data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
        try {
            metadata.put("wavelength", wavelength_);
            metadata.put("exposure", studio_.core().getExposure()); //This must happen after we have set our exposure.
            JSONArray times = new JSONArray();
            for (int i=0; i<numFrames_; i++) {
                while (studio_.core().getRemainingImageCount() < 1) { //Wait for an image to be ready
                    Thread.sleep(10);
                }
                TaggedImage taggedIm = studio_.core().popNextTaggedImage();
                times.put(Double.parseDouble((String) taggedIm.tags.get("ElapsedTime-ms"))); //Convert to float and save to json array.
                Image im = studio_.data().convertTaggedImage(taggedIm);
                Coords newCoords = im.getCoords().copyBuilder().t(i).build();
                im = im.copyAtCoords(newCoords);
                pipeline.insertImage(im); //Add image to the data pipeline for processing
                im = pipeline.getDatastore().getImage(newCoords); //Retrieve the processed image.
                imSaver.queue.put(im);
                album_.addImage(im);
            }
            metadata.put("times", times);
            imSaver.setMetadata(metadata);
        } catch (Exception e) {
            ReportingUtils.showError(e);
        }
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        if (Files.isDirectory(Paths.get(savePath).resolve("DYN_Cell" + String.valueOf(cellNum)))){
            ReportingUtils.showError("DYN_Cell " + cellNum + " already exists.");
            throw new FileAlreadyExistsException("DYN_Cell " + cellNum + " already exists.");
        } 
        return Paths.get(savePath).resolve("DYN_Cell" + String.valueOf(cellNum)).toString();
    }
    
    @Override
    public String getFilePrefix() {
        return "dyn";
    }
    
    @Override
    public int getExpectedFrames() {
        return numFrames_;
    }
}
