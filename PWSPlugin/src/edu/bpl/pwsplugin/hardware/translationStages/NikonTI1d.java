/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DeviceType;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 *
 * @author nicke
 */
public class NikonTI1d extends TranslationStage1d {
    private final TranslationStage1dSettings settings;
    private boolean calibrated = false;
    private final String devName;
    private final String pfsStatusName;
    private final String pfsOffsetName;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Status PFSStatus_;
    private final int MAX_PFS_OFFSET = 750; //I measured this as 753.025, weird, I'll just use 750
    private double[] coef_; //Should be 3 elements giving the quadratic fit of x: um, y: offset. stored in order [intercept, linear, quadratic]
    //private final FocusLockWatcher fl;

    public NikonTI1d(TranslationStage1dSettings settings) throws MMDeviceException {
        this.settings = settings;
        this.devName = settings.deviceName;
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
        
        //fl = new FocusLockWatcher(); //run thread to watch focus status. //Globals.mm().events().registerForEvents(this); //Register for microanager events.
    }
    
    private void setPFSOffset(double offset) throws MMDeviceException, InterruptedException {
        try {
            Globals.core().setProperty(pfsOffsetName, "Position", offset);
            while (busy()) { Thread.sleep(10); } //block until refocused.
        } catch (InterruptedException | MMDeviceException ee) {
            throw ee;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    private double getPFSOffset() throws MMDeviceException {
        try {
            return Double.valueOf(Globals.core().getProperty(pfsOffsetName, "Position"));
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    private Status getPFSStatus() throws MMDeviceException {
        try {
            return Status.fromString(Globals.core().getProperty(pfsStatusName, "Status"));
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    private void calibrate() throws MMDeviceException, InterruptedException {
        //move pfs offset and measure zstage to calibrate pfsConversion.
        List<WeightedObservedPoint> observations = new ArrayList<>();
        double origOffset = this.getPFSOffset(); //It is vital that we go back to this settings at the end.
        this.setPFSOffset(100);
        //Thread.sleep(100);
        //while (busy()) { Thread.sleep(10); }
        try {
            Globals.core().fullFocus();
            this.setAutoFocusEnabled(true);
            double zOrig = 0; //This will actually get initialized on the first iteration.
            for (int offset=0; offset<MAX_PFS_OFFSET; offset+=(MAX_PFS_OFFSET/4)-1) { //TODO see how well this works and reduce the number of iterations to speed it up. maybe we don't need to go to max offset.
                this.setPFSOffset(offset); 
                //while (!(getPFSStatus() == Status.FOCUSING)) { Thread.sleep(10); } //Sometimes there is a delay, but at some point the status must chnage to "focusing"
                //while (!(getPFSStatus() == Status.LOCKED)) { Thread.sleep(10); } //Wait until we are locked gain before measuring z position.
                if (offset==0) {
                    zOrig = this.getPosUm(); //All um measurement are relative to the measurement at pfsOffset = 0
                }
                observations.add(new WeightedObservedPoint(1, this.getPosUm() - zOrig, offset));
                System.out.println(String.format("%s, %s", this.getPosUm() - zOrig, offset));
            }
        } catch (InterruptedException | MMDeviceException ie) {
            throw ie;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        PolynomialCurveFitter regression = PolynomialCurveFitter.create(2);
        this.coef_ = regression.fit(observations);
        this.setPFSOffset(origOffset);
        calibrated = true;
    }
    
    private double offsetToRelPos(double offset) {
        //Uses the positive quadratic formula to invert the curve fit.
        double numerator = (-coef_[1] + Math.sqrt(Math.pow(coef_[1], 2) - 4*coef_[2]*(coef_[0]-offset)));
        double denominator = (2*coef_[2]);
        return numerator / denominator;
    }
    
    private double getOffsetForMicron(double currentOffset, double relUm) throws IllegalArgumentException {
        //Use our PFS conversion coefficients to determine what the new PFS offset should be to achieve a relative movement in microns.
        double currentRelMicron = offsetToRelPos(currentOffset);
        double finalRelMicron = currentRelMicron + relUm;
        double newOffset = new PolynomialFunction(coef_).value(finalRelMicron);
        if ((newOffset > MAX_PFS_OFFSET) || (newOffset < 0)) {
            throw new IllegalArgumentException(String.format("PFS offset of %f is out of bounds", newOffset));
        }
        return newOffset;
    }
    
    @Override
    public void setPosRelativeUm(double um) throws MMDeviceException, InterruptedException {
        try {
            if (this.getAutoFocusEnabled()) {
                if (!calibrated) { this.calibrate(); }
                double currentOffset = getPFSOffset();
                double newOffset = this.getOffsetForMicron(currentOffset, um); //TODO, if this doesn't work well enough just recurse it 2 or 3 times.
                this.setPFSOffset(newOffset);
            } else {
                Globals.core().setRelativePosition(devName, um); 
                while (busy()) { Thread.sleep(10); }
            }
            while (this.busy()) {Thread.sleep(10); } //wait for it to finish focusing.
        } catch (InterruptedException | MMDeviceException ee) {
            throw ee;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }         
    }
    
    public boolean busy() throws MMDeviceException{
        boolean zStageBusy;
        try {
            zStageBusy = Globals.core().deviceBusy(this.devName);
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
    public void setPosUm(double um) throws MMDeviceException, InterruptedException {
        try {
            if (this.getAutoFocusEnabled()) {
                if (!calibrated) { this.calibrate(); }
                double currentUm = this.getPosUm();
                double relativeUm = um - currentUm;
                this.setPosRelativeUm(relativeUm); // PFS can only be adjusted in a relative context.
            } else {
                Globals.core().setPosition(devName, um);
                while (busy()) { Thread.sleep(10); }
            }    
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }

    }
    
    @Override
    public double getPosUm() throws MMDeviceException {
        try {
            double val = Globals.core().getPosition(devName);
            return val;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }         
    }
    
    @Override
    public boolean hasAutoFocus() {
        return true;
    }
    
    @Override
    public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
        try {
            Globals.core().enableContinuousFocus(enable);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean getAutoFocusEnabled() throws MMDeviceException {
        try {
            return Globals.core().isContinuousFocusEnabled();
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    public enum Status {
        //An enumerator representing the possible PFS statuses
        LOCKED("Locked in focus"),
        FOCUSING("Focusing"),
        INRANGE("Within range of focus search"),
        OUTRANGE("Out of focus search range");
          
        private final String text;
        Status(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }   
        
        public static Status fromString(String str) {
            for (Status s : Status.values()){
                if (s.toString().equals(str)) {
                    return s;
                }
            }
            return null;
        }
    }
    
    //TODO check if objective changed. and make sure to recalibrate.
    
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
        //TODO is this actually faster than using the events?
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