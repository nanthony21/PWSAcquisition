/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;

/**
 *
 * @author N2-LiveCell
 */
public abstract class ImagingConfiguration {
    public abstract boolean hasTunableFilter();
    public abstract Camera camera();
    public abstract TunableFilter tunableFilter();
    
    public static ImagingConfiguration getInstance(Types type) {
        if (type == Types.LCTFWithHam) {
            return new LCTFWithHam();
        } else {
            return null; //This shouldn't ever happen.
        }
    }
    
    public enum Types {
        LCTFWithHam;
    }
}

class LCTFWithHam extends ImagingConfiguration {
    Camera _cam;
    TunableFilter _filt;
    
    public LCTFWithHam() {
        _cam = Camera.getInstance(Camera.Types.HAMAMATSUORCA4V3);
        _filt = TunableFilter.getInstance(TunableFilter.Types.VARISPECLCTF);
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