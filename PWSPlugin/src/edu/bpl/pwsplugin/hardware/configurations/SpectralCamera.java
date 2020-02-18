/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;

/**
 *
 * @author N2-LiveCell
 */
class SpectralCamera extends ImagingConfiguration {
    Camera _cam;
    TunableFilter _filt;
    
    public SpectralCamera(PWSPluginSettings.HWConfiguration.CamSettings camSettings, PWSPluginSettings.HWConfiguration.TunableFilterSettings filtSettings) {
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
}
