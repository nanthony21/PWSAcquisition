///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
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
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.ImageSaver;
import edu.bpl.pwsplugin.FileSpecs;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.metadata.FluorescenceMetadata;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.Pipeline;
import org.micromanager.data.internal.DefaultMetadata;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
class MultipleFluorescenceAcquisition extends ListAcquisitionBase<FluorSettings> { //Some weird stuff can happen with the display when different iterations have different resolutions. that's ok though.
    //Acquires multiple fluorescence images from a list of fluorescence settings.
    private FluorSettings settings;
    private ImagingConfiguration imConf;
    private Map<String, String> initialFilters; //This map contains all initial configuration states for the the configuration groups to adjust fluorescence filter. This is populated during initialization and then used during finalization.
    
    public MultipleFluorescenceAcquisition(PWSAlbum display) {
        super(display);
    }
    
    @Override
    protected void setCurrentSettings(FluorSettings settings) {
        this.settings = settings;
        this.imConf = Globals.getHardwareConfiguration().getImagingConfigurationByName(this.settings.imConfigName);
    }
    
    
    @Override
    protected Integer numFrames() {
        return 1;
    }
    
    @Override
    protected ImagingConfiguration getImgConfig() {
        return this.imConf;
    }
    
    @Override
    protected FileSpecs.Type getFileType() {
        return FileSpecs.Type.FLUORESCENCE;
    }
    
    @Override
    protected void finalizeAcquisitions() throws MMDeviceException {
        //Re-set all fluorescence filters to initial state.
        for (Map.Entry<String, String> entry : initialFilters.entrySet()) {
            String fluorConfigGroup = entry.getKey();
            String configState = entry.getValue();
            if (fluorConfigGroup.equals(ImagingConfigurationSettings.MANUALFLUORESCENCENAME)) { //Manual filter control
                ReportingUtils.showMessage("Return to the initial filter block and click `OK`.", Globals.frame()); 
            } else { //Automatic filter control
                try {
                    Globals.core().setConfig(fluorConfigGroup, configState);
                    Globals.core().waitForConfig(fluorConfigGroup, configState); // Wait for the device to be ready.
                } catch (Exception e) {
                    throw new MMDeviceException(e);
                }
            }
        }
    }
    
    @Override
    protected void initializeAcquisitions(List<FluorSettings> settingsList) throws MMDeviceException {
        //Imaging configuration isn't set at this point. A single set of acquisitions may contain multiple imaging configurations so we need to consider initialization for each one.
        initialFilters = new HashMap<>();
        for (FluorSettings settings : settingsList) {
            ImagingConfiguration imagingConf = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.imConfigName);
            String confGroup = imagingConf.getFluorescenceConfigGroup();
            if (confGroup != null) {
                String filt;
                try {
                    filt = Globals.core().getCurrentConfig(confGroup);
                } catch (Exception e) {
                    throw new MMDeviceException(e);
                }
                initialFilters.put(confGroup, filt);
            } else {
                initialFilters.put(ImagingConfigurationSettings.MANUALFLUORESCENCENAME, null);
            }
        }
    }

    @Override
    protected void runSingleImageAcquisition(ImageSaver imSaver, MetadataBase metadata) throws Exception {
        Globals.logger().logDebug(String.format("Multiple Fluorescence Acquisition beginning. %s", this.settings.toJsonString()));
        boolean spectralMode = imConf.hasTunableFilter();
        String fluorConfigGroup = imConf.getFluorescenceConfigGroup();
        if (fluorConfigGroup != null) {
            Globals.core().setConfig(fluorConfigGroup, this.settings.filterConfigName);
            Globals.core().waitForConfig(fluorConfigGroup, this.settings.filterConfigName); // Wait for the device to be ready.
        } else {
            ReportingUtils.showMessage("Set the correct fluorescence filter and click `OK`.");
        }
        if (spectralMode) { 
            imConf.tunableFilter().setWavelength(settings.tfWavelength);
        }
        imConf.zStage().setPosRelativeUm(settings.focusOffset);
        try {
            imSaver.beginSavingThread();
            imConf.camera().setExposure(settings.exposure);
            Globals.core().clearCircularBuffer();
            Image img = imConf.camera().snapImage();
            metadata.setMicroManagerMetadata(img);
            Integer wv;
            if (spectralMode) { wv = settings.tfWavelength; } else { wv = null; }
            FluorescenceMetadata flmd = new FluorescenceMetadata(metadata, settings.filterConfigName, imConf.camera().getExposure(), wv); //This must happen after we have set our exposure.
            Pipeline pipeline = Globals.mm().data().copyApplicationPipeline(Globals.mm().data().createRAMDatastore(), true); //The on-the-fly processor pipeline of micromanager (for image rotation, flatfielding, etc.)
            Coords coords = img.getCoords();
            pipeline.insertImage(img); //Add image to the data pipeline for processing
            img = pipeline.getDatastore().getImage(coords); //Retrieve the processed image. 
            imSaver.setMetadata(flmd);
            this.displayImage(img);
            imSaver.addImage(img);
        } finally {
            imConf.zStage().setPosRelativeUm(-settings.focusOffset);
            Globals.logger().logDebug("Multiple Fluorescence Acquisition ending.");
        }
    }
}
