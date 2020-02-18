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
public abstract class ImagingConfiguration {
    public abstract boolean hasTunableFilter();
    public abstract Camera camera();
    public abstract TunableFilter tunableFilter();
    
    public static ImagingConfiguration getInstance(PWSPluginSettings.HWConfiguration.ImagingConfigurationSettings settings) {
        if (settings.type == Types.SpectralCamera) {
            return new SpectralCamera(settings.camSettings, settings.filtSettings);
        } else if (settings.type == Types.StandardCamera) {
            return new StandardCamera(settings.camSettings);
        } else {
            return null; //This shouldn't ever happen.
        }
    }
    
    public enum Types {
        SpectralCamera,
        StandardCamera;
    }
}

