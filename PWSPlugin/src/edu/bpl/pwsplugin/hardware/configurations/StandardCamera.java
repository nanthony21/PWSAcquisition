/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.CamSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;

/**
 *
 * @author N2-LiveCell
 */
public class StandardCamera extends ImagingConfiguration {
    Camera _cam;
    
    public StandardCamera(ImagingConfigurationSettings settings) {
        super(settings);
        _cam = Camera.getInstance(settings.camSettings);
    }
    
    @Override
    public boolean hasTunableFilter() { return false; }
    
    @Override
    public Camera camera() { return _cam; }
    
    @Override
    public TunableFilter tunableFilter() { return null; } //TODO should this throw an exception instead?
}
