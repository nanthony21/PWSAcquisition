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
public abstract class NikonTIBase extends TranslationStage1d {
    protected final TranslationStage1dSettings settings;
    private boolean calibrated = false;
    //private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private double[] coef_; //Should be 3 elements giving the quadratic fit of x: um, y: offset. stored in order [intercept, linear, quadratic]
    //private final FocusLockWatcher fl;

    public NikonTIBase(TranslationStage1dSettings settings) throws MMDeviceException {
        this.settings = settings;        
    }
    
    protected abstract void setPFSOffset(double offset) throws MMDeviceException, InterruptedException;
    
    protected abstract double getPFSOffset() throws MMDeviceException;
    
    protected abstract Status getPFSStatus() throws MMDeviceException ;
    
    protected abstract Double getMaximumPFSOffset();
    
    @Override
    public abstract boolean identify();
    
    @Override
    public abstract List<String> validate();
    
    
    private void calibrate() throws MMDeviceException, InterruptedException {
        //move pfs offset and measure zstage to calibrate pfsConversion.
        List<WeightedObservedPoint> observations = new ArrayList<>();
        double origOffset = this.getPFSOffset(); //It is vital that we go back to this settings at the end.
        this.setPFSOffset(getMaximumPFSOffset()/20); //Move somewhere close to the starting point (0) but not quite.
        try {
            Globals.core().fullFocus();
            this.setAutoFocusEnabled(true);
            double zOrig = 0; //This will actually get initialized on the first iteration.
            for (int offset=0; offset<getMaximumPFSOffset(); offset+=(getMaximumPFSOffset()/4)-1) {
                this.setPFSOffset(offset); 
                if (offset==0) {
                    zOrig = this.getPosUm(); //All um measurement are relative to the measurement at pfsOffset = 0
                }
                observations.add(new WeightedObservedPoint(1, this.getPosUm() - zOrig, this.getPFSOffset()));
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
        if ((newOffset > getMaximumPFSOffset()) || (newOffset < 0)) {
            throw new IllegalArgumentException(String.format("PFS offset of %f is out of bounds", newOffset));
        }
        return newOffset;
    }
    
    @Override
    public void setPosRelativeUm(double um) throws MMDeviceException, InterruptedException {
        try {
            if (this.getAutoFocusEnabled()) {
                if (!calibrated) { this.calibrate(); }
                double remainingRelUm = um; //This variable keeps track of how much further we need to go to achieve the original relative movement of `um`
                for (int i=0; i<5; i++) { //Due to calibration errors the below code is not accurate enough on one iteration. We give it up to 5 iterations to get within `tolerance` of the correct value.
                    double currentOffset = getPFSOffset();
                    double currentPos = this.getPosUm();
                    double newOffset = this.getOffsetForMicron(currentOffset, remainingRelUm);
                    this.setPFSOffset(newOffset); //This will block until the move is finished.
                    double newPos = this.getPosUm();
                    remainingRelUm -= newPos - currentPos; //subtract the delta-z from this iteration from our remaning distance to go.
                    //System.out.println(String.format("c %f, n %f, r %f, co %f, no %f", currentPos, newPos, remainingRelUm, currentOffset, newOffset));
                    if (remainingRelUm <= 0.01) { break; }//I'm just not sure how to choose the tolerance. However running through 5 iterations without satisfying this requirement is fine.
                }
            } else {
                Globals.core().setRelativePosition(settings.deviceName, um); 
            }
            while (this.busy()) {Thread.sleep(10); } //wait for it to finish focusing.
        } catch (InterruptedException | MMDeviceException ee) {
            throw ee;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }         
    }
    
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
    public void setPosUm(double um) throws MMDeviceException, InterruptedException {
        try {
            if (this.getAutoFocusEnabled()) {
                if (!calibrated) { this.calibrate(); }
                double currentUm = this.getPosUm();
                double relativeUm = um - currentUm;
                this.setPosRelativeUm(relativeUm); // PFS can only be adjusted in a relative context.
            } else {
                Globals.core().setPosition(settings.deviceName, um);
                while (busy()) { Thread.sleep(10); }
            }
        } catch (InterruptedException | MMDeviceException ee) {
            throw ee;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }

    }
    
    @Override
    public double getPosUm() throws MMDeviceException {
        try {
            return Globals.core().getPosition(settings.deviceName);
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
    
    @Override
    public boolean getAutoFocusLocked() throws MMDeviceException {
        try {
            return Globals.core().isContinuousFocusLocked();
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public void runFullFocus() throws MMDeviceException {
        try { //TODO add some smart software autofocus here so the image is actually focused.
            Globals.core().fullFocus();
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    public enum Status {
        //An enumerator representing the possible PFS statuses
        LOCKED, //Locked in focus
        FOCUSING, //Searching for focus
        INRANGE, //Could try to focus but isn't currently
        OUTRANGE; //Could not try to focus, specimen is probably out of search range.
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
