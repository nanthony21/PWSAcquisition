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

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.fileSavers.MMSaver;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.metadata.DynamicsMetadata;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.TaggedImage;
import mmcorej.org.json.JSONArray;
import mmcorej.org.json.JSONObject;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.internal.utils.ReportingUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


class DynAcqManager implements AcquisitionManager{
    double exposure_; //The camera exposure in milliseconds.
    int wavelength_; //The wavelength to acquire images at
    int numFrames_; //The number of images to acquire.
    PWSAlbum album_;
    DynSettings settings;
    
    public DynAcqManager(PWSAlbum album){
        album_ = album;
    }
    
    public void setSequenceSettings(DynSettings settings) {
        this.exposure_ = settings.exposure;
        this.wavelength_ = settings.wavelength;
        this.numFrames_ = settings.numFrames;
        this.settings = settings;
    }
    
    @Override
    public void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, MetadataBase metadata) {
        ImagingConfiguration conf = Globals.getHardwareConfiguration().getConfigurationByName(this.settings.imConfigName);
        Camera camera = conf.camera();
        TunableFilter tunableFilter = conf.tunableFilter();
        try {album_.clear();} catch (IOException e) {ReportingUtils.logError(e, "Error from PWSALBUM");}
        try {
            tunableFilter.setWavelength(wavelength_);
            camera.setExposure(exposure_);
            Globals.core().setCircularBufferMemoryFootprint(1000); //increase the circular buffer to 1Gb to avoid weird issues with lost images
            Globals.core().clearCircularBuffer();
            camera.startSequence(numFrames_, 0, false);
        } catch (Exception e) {
            ReportingUtils.showError(e);
        }
        Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
        try {
            MMSaver imSaver = new MMSaver(this.getSavePath(savePath, cellNum), imagequeue, this.getExpectedFrames(), this.getFilePrefix());
            imSaver.start();
            List<Double> times = new ArrayList<>();
            for (int i=0; i<numFrames_; i++) {
                while (Globals.core().getRemainingImageCount() < 1) { //Wait for an image to be ready
                    Thread.sleep(10);
                }
                TaggedImage taggedIm = Globals.core().popNextTaggedImage();
                times.add(Double.parseDouble((String) taggedIm.tags.get("ElapsedTime-ms"))); //Convert to float and save to json array.
                Image im = Globals.mm().data().convertTaggedImage(taggedIm);
                Coords newCoords = im.getCoords().copyBuilder().t(i).build();
                im = im.copyAtCoords(newCoords);
                pipeline.insertImage(im); //Add image to the data pipeline for processing
                im = pipeline.getDatastore().getImage(newCoords); //Retrieve the processed image.
                imSaver.queue.put(im);
                album_.addImage(im);
            }
            DynamicsMetadata dmd = new DynamicsMetadata(metadata, Double.valueOf(wavelength_), times, camera.getExposure());

            imSaver.setMetadata(dmd.toJson());
            imSaver.join();
        } catch (Exception e) {
            ReportingUtils.showError(e);
        }
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        Path path = Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)).resolve("Dynamics");
        if (Files.isDirectory(path)){
            throw new FileAlreadyExistsException("Cell " + cellNum + " dynamics already exists.");
        } 
        return path.toString();
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
