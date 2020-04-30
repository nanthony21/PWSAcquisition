/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import java.util.Arrays;
import java.util.List;
import mmcorej.DeviceType;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class MMConfigAdapter {
    
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
    } //Scan the hardware configuration
    //Fluorescence filters
    //Cameras

    public List<String> getFilters() {
        return this.filters;
    }

    public List<String> getConnectedCameras() {
        return this.connectedCameras;
    }
    
}
