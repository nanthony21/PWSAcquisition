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
    
    public NikonTI2_zStage(TranslationStage1dSettings settings) {
        super(settings);
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
    protected void setPFSOffset(double offset) throws MMDeviceException, InterruptedException {
        try {
            Globals.core().setAutoFocusOffset(offset);
            while (busy()) { Thread.sleep(10); } //block until refocused.
        } catch (InterruptedException | MMDeviceException ee) {
            throw ee;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    protected double getPFSOffset() throws MMDeviceException {
        try {
            return Globals.core().getAutoFocusOffset();
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
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
                    (Globals.core().getDeviceLibrary(settings.deviceName).equals("NikonTI2")));
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
