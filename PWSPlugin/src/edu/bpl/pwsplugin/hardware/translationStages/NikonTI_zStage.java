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

    public NikonTI_zStage(TranslationStage1dSettings settings) throws MMDeviceException, IDException {
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
    protected String getZDriveDeviceName() {
        return settings.deviceName;
    }
    
    @Override
    protected String getPFSDeviceName() {
        return pfsStatusName;
    }
    
    private Status getPFSStatus() throws MMDeviceException {
        String statusStr;
        try {
            statusStr = Globals.core().getProperty(pfsStatusName, "Status");
            switch (statusStr) {
                case ("Locked in focus"):
                    return Status.LOCKED;
                case ("Focusing"):
                    return Status.FOCUSING;
                case ("Within range of focus search"):
                    return Status.INRANGE;
                case ("Out of focus search range"):
                    return Status.OUTRANGE;
                case ("Focus lock failed"):
                    return Status.LOCKFAILED;
            }
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        //If we got this far then the status string must not have been recognized;
        throw new RuntimeException(String.format("Status string (%s) was not recognized.", statusStr));
    }
    
    @Override
    protected boolean busy() throws MMDeviceException{
        boolean zStageBusy;
        try {
            zStageBusy = Globals.core().deviceBusy(settings.deviceName);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        if (this.getAutoFocusEnabled()) {
            return ((this.getPFSStatus() == Status.FOCUSING) || zStageBusy);
        } else {
            return zStageBusy;
        }
    }
    
    @Override
    public boolean identify() {
        try {
            return ((Globals.core().getDeviceName(settings.deviceName).equals("TIZDrive"))
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
    
    public enum Status {
        //An enumerator representing the possible PFS statuses
        LOCKED, //Locked in focus
        FOCUSING, //Searching for focus
        INRANGE, //Could try to focus but isn't currently
        OUTRANGE, //Could not try to focus, specimen is probably out of search range.
        LOCKFAILED; //This can appear when PFS was locked but movement of the sample broke the lock.
    }
    
    //TODO check if objective changed. and make sure to recalibrate. Unfortunately the config group changed event is broken.
    
    /*@Subscribe
    public void focusChanged(PropertyChangedEvent evt) {
        try {
            if (evt.getProperty().equals("Status") && evt.getDevice().equals(pfsStatusName)) {
                Status currentState = Status.fromString(evt.getValue());
                pcs.firePropertyChange("focusLock", PFSStatus_, currentState); //When the pfs lock status is changed we fire an event to our listeners.
                PFSStatus_ = currentState; //Note this event based property change detection can be slow. to quickly check PFS status use the getPFSStatus method rather than using this variable.
            }
        } catch (Exception e) {
            Globals.mm().logs().logError(e);
        }
    }

    @Override    
    public void addFocusLockListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener("focusLock", listener);
    }
    

    class FocusLockWatcher implements Runnable {
        //is this actually faster than using the events?
        private final ScheduledExecutorService exc = Executors.newSingleThreadScheduledExecutor();
        private Status currentStatus;
        
        public FocusLockWatcher() {
            long period = 10; //100hz
            exc.scheduleAtFixedRate(this, 10, period, TimeUnit.DAYS); //initial delay to avoid issue with leaking this in constructor.
        }

        @Override
        public void run() {
            //boolean run = true;
            //while (run) {
            try {
                iterate();
            } catch (MMDeviceException e) {
                Globals.mm().logs().logError(e);
            }
            //}
        }

        private void iterate() throws MMDeviceException {
            Status status = NikonTI1d.this.getPFSStatus();
            if (status != currentStatus) {
                emitStatusChanged(status, currentStatus);
            }
            currentStatus = status;
        }
        
        private void emitStatusChanged(Status newStatus, Status oldStatus) {
            NikonTI1d.this.pcs.firePropertyChange("focusLock", oldStatus, newStatus);
        }

    }
    */
}