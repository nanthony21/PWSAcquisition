/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.settings.HWConfiguration;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.Arrays;
import java.util.List;
import mmcorej.CMMCore;
import mmcorej.DeviceType;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author LCPWS3
 */
public class Globals {
    private static Studio studio_ = null;
    private static AcqManager acqMan_;
    private static HWConfiguration config;
    private static MMConfigAdapter mmAdapter;
    
    public static void init(Studio studio) {
        studio_ = studio;
        mmAdapter = new MMConfigAdapter();
        acqMan_ = new AcqManager();
        setHardwareConfiguration(new HWConfiguration()); //Very important that this is instantiated after studio. this is probably bad design actually.
    }
            
    public static Studio mm() {
        return studio_;
    }
    
    public static CMMCore core() {
        return studio_.core();
    }
    
    public static AcqManager acqManager() {
        return acqMan_;
    }
    
    public static void setHardwareConfiguration(HWConfiguration configg) {
        config = configg;
        acqMan_.setHWConfiguration(configg);    
    }
    
    public static HWConfiguration getHardwareConfiguration() {
        return config;
    }
    
    public static MMConfigAdapter getMMConfigAdapter() {
        return mmAdapter;
    }

    public static class MMConfigAdapter {
        List<String> filters;
        List<String> connectedCameras;
        public boolean autoFilterSwitching;

        public MMConfigAdapter() {
            //Scan the hardware configuration
            //Fluorescence filters
            this.filters = Arrays.asList(Globals.core().getAvailableConfigs("Filter").toArray());
            if (this.filters.isEmpty()) {
                this.autoFilterSwitching = false;
                ReportingUtils.showMessage("Micromanager is missing a `Filter` config group which is needed for automated fluorescence. The first setting of the group should be the filter block used for PWS");
            } else {
                this.autoFilterSwitching = true;
            }
            //Cameras
            this.connectedCameras = Arrays.asList(Globals.core().getLoadedDevicesOfType(DeviceType.CameraDevice).toArray());                      
        }

        public List<String> getFilters(){
            return this.filters;
        }

        public List<String> getConnectedCameras() {
            return this.connectedCameras;
        }
    }
}