/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class StandardCamera extends DefaultImagingConfiguration {
    Camera _cam;
    Illuminator _illum;
    
    protected StandardCamera(ImagingConfigurationSettings settings) throws MMDeviceException {
        super(settings);
        _cam = Camera.getAutomaticInstance(settings.camSettings);
        _illum = Illuminator.getAutomaticInstance(settings.illuminatorSettings);
        if ((_illum==null) || (_cam==null)) {
            throw new MMDeviceException("StandardCamera failed to initialize");
        }
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
    public List<String> validate() throws MMDeviceException {
        List<String> errs = super.validate();
        errs.addAll(this._cam.validate());
        errs.addAll(this._illum.validate());
        errs.addAll(this.zStage().validate());
        return errs;
    }
}
