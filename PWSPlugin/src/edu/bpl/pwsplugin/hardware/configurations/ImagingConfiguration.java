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
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import edu.bpl.pwsplugin.hardware.translationStages.TranslationStage1d;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public abstract class ImagingConfiguration {
    ImagingConfigurationSettings settings;
    private boolean initialized_ = false;
    private TranslationStage1d zStage;
    private boolean activated_ = false;
    
    protected ImagingConfiguration(ImagingConfigurationSettings settings) {
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
    
    public TranslationStage1d zStage() {
        return zStage;
    }
    
    private void initialize() throws MMDeviceException { //One-time initialization of devices
        zStage = TranslationStage1d.getAutomaticInstance();
        if (zStage == null) {
            throw new MMDeviceException("No supported Z-stage was found.");
        }
        camera().initialize();
        if (hasTunableFilter()) {
            tunableFilter().initialize();
        }
        illuminator().initialize();
        initialized_ = true;
    }
    
    //We only want the following functions to be accessed by the HWConfigrartion
    
    void activateConfiguration() throws MMDeviceException { //Actually configure the hardware to use this configuration.
        if (!initialized_) {
            this.initialize(); //If we haven't yet then run the one-time initialization for the the devices.
        }
        camera().activate();
        if (hasTunableFilter()) {
            tunableFilter().activate();
        }
        illuminator().activate();
        try {
            Globals.core().setConfig(settings.configurationGroup, settings.configurationName);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MMDeviceException(ie);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        activated_ = true;
    }
    
    void deactivateConfiguration() {
        activated_ = false;
    }
    
    boolean isActive() throws MMDeviceException {
        if (!activated_) { return false; }
        try { //Even if the `activated_` flag is true we still check that the configuration group is properly set, just to make sure.
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

