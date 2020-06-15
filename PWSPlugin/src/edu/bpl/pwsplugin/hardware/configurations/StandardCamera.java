/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class StandardCamera extends ImagingConfiguration {
    Camera _cam;
    Illuminator _illum;
    
    protected StandardCamera(ImagingConfigurationSettings settings) {
        super(settings);
        _cam = Camera.getInstance(settings.camSettings);
        _illum = Illuminator.getInstance(settings.illuminatorSettings);
    }
    
    @Override
    public boolean hasTunableFilter() { return false; }
    
    @Override
    public Camera camera() { return _cam; }
    
    @Override
    public TunableFilter tunableFilter() { throw new UnsupportedOperationException("StandardCamera configuration has no tunable filter"); }
    
    @Override
    public Illuminator illuminator() { return _illum; }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        errs.addAll(this._cam.validate());
        errs.addAll(this._illum.validate());
        return errs;
    }
}
