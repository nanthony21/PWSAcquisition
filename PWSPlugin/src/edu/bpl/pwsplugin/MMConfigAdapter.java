package edu.bpl.pwsplugin;

import java.util.Arrays;
import java.util.List;
import mmcorej.DeviceType;


public class MMConfigAdapter {
    //Scans the Micro-Manager hardware configuration to provide useful information to the PWS plugin.
    private List<String> filters;
    private List<String> connectedCameras;
    private List<String> connectedShutters;
    public boolean autoFilterSwitching;

    public MMConfigAdapter() {
        this.refresh();
    }

    public void refresh() {
        //Scan the hardware configuration
        //Fluorescence filters
        this.filters = Arrays.asList(Globals.core().getAvailableConfigs("Filter").toArray());
        if (this.filters.isEmpty()) {
            this.autoFilterSwitching = false;
            Globals.mm().logs().showMessage("Micromanager is missing a `Filter` config group which is needed for automated fluorescence. The first setting of the group should be the filter block used for PWS");
        } else {
            this.autoFilterSwitching = true;
        }
        //Cameras
        this.connectedCameras = Arrays.asList(Globals.core().getLoadedDevicesOfType(DeviceType.CameraDevice).toArray());
        //Shutters (illuminators
        this.connectedShutters = Arrays.asList(Globals.core().getLoadedDevicesOfType(DeviceType.ShutterDevice).toArray());
    }
    
    public List<String> getFilters() {
        return this.filters;
    }

    public List<String> getConnectedCameras() {
        return this.connectedCameras;
    }
    
    public List<String> getConnectedShutters() {
        return this.connectedShutters;
    }
    
}
