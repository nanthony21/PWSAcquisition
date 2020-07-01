package edu.bpl.pwsplugin.hardware;

import edu.bpl.pwsplugin.Globals;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mmcorej.DeviceType;


public class MMConfigAdapter {
    //Scans the Micro-Manager hardware configuration to provide useful information to the PWS plugin.
    private List<String> filters;
    public boolean autoFilterSwitching;
    private final List<ActionListener> onRefreshListeners = new ArrayList<>();

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

        //Fire action listeners
        for (ActionListener l : this.onRefreshListeners) {
            l.actionPerformed(null);
        }
    }
    
    public void addRefreshListener(ActionListener listener) {
        this.onRefreshListeners.add(listener);
    }
    
    public List<String> getFilters() {
        if (this.filters.isEmpty()) {
            List<String> l = new ArrayList<>();
            l.add("None!");
            return l;
        } else {
            return this.filters;
        }
    }

    public List<String> getConnectedCameras() {
        return Arrays.asList(Globals.core().getLoadedDevicesOfType(DeviceType.CameraDevice).toArray());
    }
    
    public List<String> getConnectedShutters() {
        return Arrays.asList(Globals.core().getLoadedDevicesOfType(DeviceType.ShutterDevice).toArray());
    }
    
    public List<String> getAllDevices() {
        return Arrays.asList(Globals.core().getLoadedDevicesOfType(DeviceType.AnyType).toArray());
    }
    
}
