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
public class NikonTI_zStage extends NikonTIBase {
    private final String pfsStatusName;
    private final String pfsOffsetName;

    public NikonTI_zStage(TranslationStage1dSettings settings) throws MMDeviceException {
        super(settings);
        try {
            String nikonHub = null;
            for (String hubLabel : Globals.core().getLoadedDevicesOfType(DeviceType.HubDevice)) {
                if (Globals.core().getDeviceName(hubLabel).equals("TIScope")) {
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
                if (name.equals("TIPFSOffset")) {
                    offsetName = devLabel;
                } else if (name.equals("TIPFSStatus")) {
                    statusName = devLabel;
                }
            }
            if (offsetName==null || statusName==null) {
                throw new MMDeviceException("PFS devices were not found.");
            }
            pfsOffsetName = offsetName;
            pfsStatusName = statusName;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    protected Double getMaximumPFSOffset() {
        return 753.025; //I measured this as 753.025.
    }
    
    @Override
    protected Double getMinimumPFSOffset() {
        return 0.0;
    }
    
    @Override
    protected String getPFSOffsetDeviceName() {
        return pfsOffsetName;
    }
    
    @Override
    protected NikonTIBase.Status getPFSStatus() throws MMDeviceException {
        String statusStr;
        try {
            statusStr = Globals.core().getProperty(pfsStatusName, "Status");
            switch (statusStr) {
                case ("Locked in focus"):
                    return NikonTIBase.Status.LOCKED;
                case ("Focusing"):
                    return NikonTIBase.Status.FOCUSING;
                case ("Within range of focus search"):
                    return NikonTIBase.Status.INRANGE;
                case ("Out of focus search range"):
                    return NikonTIBase.Status.OUTRANGE;
            }
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        //If we got this far then the status string must not have been recognized;
        throw new RuntimeException(String.format("Status string (%s) was not recognized.", statusStr));
    }
    
    @Override
    public boolean identify() {
        try {
            return ((Globals.core().getDeviceName(settings.deviceName).equals("ZStage"))
                    &&
                    (Globals.core().getDeviceLibrary(settings.deviceName).equals("NikonTI")));
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        if (!identify()) {
            errs.add(String.format("Device %s is not recognized as a Nikon TI Z-stage", settings.deviceName));
        }
        return errs;
    }
}