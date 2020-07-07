/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import java.util.List;

/**
 *
 * @author nick
 */
public interface ImagingConfiguration {

    Camera camera();

    Illuminator illuminator();
    
    String objectConfigGroup();

    ImagingConfigurationSettings settings();

    boolean hasTunableFilter();
    
    TunableFilter tunableFilter();

    List<String> validate();

    TranslationStage1d zStage();
    
    public static ImagingConfiguration getInstance(ImagingConfigurationSettings settings) {
        if (null == settings.configType) {
            return null; //This shouldn't ever happen.
        } else switch (settings.configType) {
            case SpectralCamera:
                return new SpectralCamera(settings);
            case StandardCamera:
                return new StandardCamera(settings);
            default:
                return null; //This shouldn't ever happen.
        }
    }
    
    public enum Types {
        SpectralCamera,
        StandardCamera;
    }
    
}
