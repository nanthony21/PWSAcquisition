/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.configurations;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
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
    
    public void activateConfiguration() throws MMDeviceException { //Actually configure the hardware to use this configuration.
        try {
            Globals.core().setConfig(settings.configurationGroup, settings.configurationName);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MMDeviceException(ie);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    public boolean isActive() throws MMDeviceException {
        try {
            boolean active = Globals.core().getCurrentConfig(settings.configurationGroup).equals(settings.configurationName);
            return active;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MMDeviceException(ie);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
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

