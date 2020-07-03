/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DeviceType;

/**
 *
 * @author nicke
 */
public class NikonTI2_zStage extends NikonTIBase {
    private final String offsetDevice; //A stage device with no properties
    private final String pfsDevice; //An autofocus device
            
    public NikonTI2_zStage(TranslationStage1dSettings settings) throws MMDeviceException, IDException {
        super(settings);
        try {
            String nikonHub = null;
            for (String hubLabel : Globals.core().getLoadedDevicesOfType(DeviceType.HubDevice)) {
                if (Globals.core().getDeviceLibrary(hubLabel).equals("NikonTi2")) {
                    nikonHub = hubLabel;
                    break;
                }
            }
            if (nikonHub == null) { 
                throw new MMDeviceException("No Nikon Hub device was found."); 
            }
            String offsetName=null; String statusName=null;
            for (String devLabel : Globals.core().getLoadedPeripheralDevices(nikonHub)) {
                String name;
                name = Globals.core().getDeviceName(devLabel);
                if (name.equals("PFSOffset")) {
                    offsetName = devLabel;
                } else if (name.equals("PFS")) {
                    statusName = devLabel;
                }
            }
            if (offsetName==null || statusName==null) {
                throw new MMDeviceException("PFS devices were not found.");
            }
            offsetDevice = offsetName;
            pfsDevice = statusName;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    protected Double getMaximumPFSOffset() {
        return 32500.0;
    }
    
    @Override
    protected Double getMinimumPFSOffset() {
        return 1.0;
    }
    
    @Override
    protected String getPFSOffsetDeviceName() {
        return offsetDevice;
    }
    
    @Override
    protected boolean busy() throws MMDeviceException {
        try {
            return ((Globals.core().deviceBusy(offsetDevice))
                    ||
                    (Globals.core().deviceBusy(pfsDevice))
                    ||
                    (Globals.core().deviceBusy(settings.deviceName)));
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean identify() {
        try {
            return ((Globals.core().getDeviceName(settings.deviceName).equals("ZDrive"))
                    &&
                    (Globals.core().getDeviceLibrary(settings.deviceName).equals("NikonTi2")));
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        if (!identify()) {
            errs.add(String.format("Device %s is not recognized as a Nikon TI2 Z-stage", settings.deviceName));
        }
        return errs;
    }
}
