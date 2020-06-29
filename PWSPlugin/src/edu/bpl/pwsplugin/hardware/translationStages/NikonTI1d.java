/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.translationStages;

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import mmcorej.DeviceType;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.micromanager.events.PropertyChangedEvent;

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
    private Status PFSStatus;
    private final int MAX_PFS_OFFSET = 750; //I measured this as 753.025, weird, I'll just use 750
    private double[] pfsOffsetConversion; //Should be 3 elements giving the quadratic fit of x: um, y: offset. stored in order [intercept, linear, quadratic]

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
        
        Globals.mm().events().registerForEvents(this); //Register for microanager events.
    }
    
    private void setPFSOffset(int offset) throws MMDeviceException {
        try {
            Globals.core().setProperty(pfsOffsetName, "Position", offset);
            Globals.core().waitForDevice(devName); //TODO block to adjustement is done. Does this work?
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    public void calibrate() throws MMDeviceException, InterruptedException {
        //move pfs offset and measure zstage to calibrate pfsConversion.
        this.setPFSOffset(0);
        try {
            Globals.core().fullFocus();
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        this.setAutoFocusEnabled(true);
        List<WeightedObservedPoint> observations = new ArrayList<>();
        double zOrig = this.getPosUm();
        for (int offset=0; offset<MAX_PFS_OFFSET; offset+=100) { //TODO see how well this works and reduce the number of iterations to speed it up. maybe we don't need to go to max offset.
            this.setPFSOffset(offset); 
            while (this.PFSStatus!=Status.FOCUSING) { Thread.sleep(10); } //Sometimes there is a delay, but at some point the status must chnage to "focusing"
            while (this.PFSStatus!=Status.LOCKED) { Thread.sleep(10); } //Wait until we are locked gain before measuring z position.
            observations.add(new WeightedObservedPoint(1, this.getPosUm() - zOrig, offset));
            System.out.println(String.format("%s, %s", this.getPosUm() - zOrig, offset));
        }
        PolynomialCurveFitter regression = PolynomialCurveFitter.create(2);
        this.pfsOffsetConversion = regression.fit(observations);
        calibrated = true;
    }
    
    public void setPosRelativeUm(double um) throws MMDeviceException {
        try {
            if (this.getAutoFocusEnabled()) {
                if (!calibrated) { this.calibrate(); }
                double deltaOffset = pfsOffsetConversion[0] + pfsOffsetConversion[1]*um + pfsOffsetConversion[2]*Math.pow(um, 2);
                double offset = Double.valueOf(Globals.core().getProperty(pfsOffsetName, "Position"));
                Globals.core().setProperty(pfsOffsetName, "Position", offset);
            } else {
                Globals.core().setRelativePosition(devName, um); //TODO do we need to block here?
            }
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
            return ((this.PFSStatus == Status.FOCUSING) || zStageBusy);
        } else {
            return zStageBusy;
        }
    }
    
    @Override
    public void setPosUm(double um) throws MMDeviceException {
        try {
            if (this.getAutoFocusEnabled()) {
                if (!calibrated) { this.calibrate(); }//throw new RuntimeException("Cannot set PFS position before calibration."); }
                double currentUm = this.getPosUm();
                double relativeUm = currentUm - um;
                this.setPosRelativeUm(relativeUm);
            } else {
                Globals.core().setPosition(devName, um);
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
    
    @Subscribe
    public void focusChanged(PropertyChangedEvent evt) {
        try {
            if (evt.getProperty().equals("Status") && evt.getDevice().equals(pfsStatusName)) {
                Status currentState = Status.fromString(evt.getValue());
                pcs.firePropertyChange("focusLock", PFSStatus, currentState);
                PFSStatus = currentState;
            }
        } catch (Exception e) {
            Globals.mm().logs().logError(e);
        }
    }
    
    @Override    
    public void addFocusLockListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener("focusLock", listener);
    }
    
    //TODO check if objective changed. and make sure to recalibrated.
}
