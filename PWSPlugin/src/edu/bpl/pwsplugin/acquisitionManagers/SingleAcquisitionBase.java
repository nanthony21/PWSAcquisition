/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DoubleVector;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author nick
 */
public abstract class SingleAcquisitionBase<S> implements Acquisition<S> {
    //A base class an acquisition manager that handles instantiation of a the required metadata for every type of image.
    //This class assumes that the same imaging configuration will be used throughout the whole imaging process, which may not be true.
   
    @Override
    public void acquireImages(String savePath, int cellNum) throws Exception {
        MetadataBase metadata = this.initializeMetadata();
        this.runImageAcquisition(savePath, cellNum, metadata);
    }
    
    private MetadataBase initializeMetadata() throws Exception {
        ImagingConfiguration imConf = this.getImgConfig(); 
        if (!imConf.isActive()) { //It's important that the configuration is activated before we try pulling metadata like the affine transform
            imConf.activateConfiguration(); //Activation must occur every time the imaging configuration changes.
        }
        if (Globals.core().getPixelSizeUm() == 0.0) { //This information gets saved to the metadata below in the form of an affine transform.
            ReportingUtils.showMessage("It is highly recommended that you provide MicroManager with a pixel size setting for the current setup. Having this information is useful for analysis.");
        }
        DoubleVector aff = Globals.core().getPixelSizeAffine();
        List<Double> trans = new ArrayList<>();
        for (int i=0; i<aff.size(); i++) {
            trans.add(aff.get(i));
        }            

        MetadataBase metadata = new MetadataBase(
                imConf.camera().getSettings().linearityPolynomial,
                Globals.getHardwareConfiguration().getSettings().systemName,
                imConf.camera().getSettings().darkCounts,
                trans);
        return metadata;
    }
    
    protected abstract void runImageAcquisition(String savePath, int cellNum, MetadataBase md) throws Exception;

    protected abstract ImagingConfiguration getImgConfig();
}
