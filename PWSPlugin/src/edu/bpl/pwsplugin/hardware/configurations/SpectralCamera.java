/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.CamSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.TunableFilterSettings;
import org.micromanager.data.Image;

/**
 *
 * @author N2-LiveCell
 */
public class SpectralCamera extends ImagingConfiguration {
    Camera _cam;
    TunableFilter _filt;
    
    public SpectralCamera(CamSettings camSettings, TunableFilterSettings filtSettings) {
        _cam = Camera.getInstance(camSettings);
        _filt = TunableFilter.getInstance(filtSettings);
    }
    
    @Override
    public boolean hasTunableFilter() { return true; }
    
    @Override
    public Camera camera() {
        return _cam;
    }
    
    @Override
    public TunableFilter tunableFilter() {
        return _filt;
    }
    
    public boolean supportsTTLSequencing() {
        return (_cam.supportsTriggerOutput() && _filt.supportsSequencing());
    }
    
    public void startTTLSequence(int numImages, double delayMs, boolean externalTriggering) throws Exception {
        if (!supportsTTLSequencing()) {
            throw new UnsupportedOperationException("This imaging configuration does not support TTL sequencing.");
        }
        if (externalTriggering) {
            _cam.startSequence(numImages, delayMs, true);
            _filt.startSequence(); //This should trigger a pulse which sets the whole thing off. 
        }
        else { //Since we're not using an external trigger we need to have the camera control the timing.
            _filt.startSequence();
            _cam.startSequence(numImages, delayMs, false);
        }
    }
    
    public Image snapImage(int wavelength) throws Exception {
        if (_filt.getWavelength() != wavelength) {
            _filt.setWavelength(wavelength);
            while (_filt.isBusy()) {Thread.sleep(1);} //Wait until the device says it is tuned  
        }
        Image im = _cam.snapImage(); //This is so slow.
        return im;
    }
    
    public void stopTTLSequence() throws Exception {
        _cam.stopSequence();
        _filt.stopSequence();
    }
}
