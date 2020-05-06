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
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public abstract class ImagingConfiguration {
    ImagingConfigurationSettings settings;
    
    public ImagingConfiguration(ImagingConfigurationSettings settings) {
        this.settings = settings;
    }
    
    public ImagingConfigurationSettings settings() {
        return settings;
    }
    
    public abstract boolean hasTunableFilter();
    public abstract Camera camera();
    public abstract TunableFilter tunableFilter();
    public abstract Illuminator illuminator();
    public abstract List<String> validate();
    
    public static ImagingConfiguration getInstance(ImagingConfigurationSettings settings) {
        if (settings.configType == Types.SpectralCamera) {
            return new SpectralCamera(settings);
        } else if (settings.configType == Types.StandardCamera) {
            return new StandardCamera(settings);
        } else {
            return null; //This shouldn't ever happen.
        }
    }
    
    public enum Types {
        SpectralCamera,
        StandardCamera;
    }
}

