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
import mmcorej.DeviceType;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.micromanager.events.PropertyChangedEvent;

/**
 *
 * @author nicke
 */
public class NikonTI1d extends TranslationStage1d {
    private final TranslationStage1dSettings settings;
    private double pfsConversionSlope = .1;
    private boolean calibrated = false;
    private String devName;
    private String pfsStatusName;
    private String pfsOffsetName;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private String oldState;
    private final int MAX_PFS_OFFSET; //TODO fill this in.

    public NikonTI1d(TranslationStage1dSettings settings) {
        this.settings = settings;
        this.devName = settings.deviceName;
        //TODO detect the pfs devices.
    }
    
    private void setPFSOffset(int offset) throws MMDeviceException {
        try {
            Globals.core().setProperty(pfsOffsetName, "Position", offset);
            Globals.core().waitForDevice(devName); //TODO block to adjustement is done. Does this work?
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    public void calibrate() throws MMDeviceException {
        //move pfs offset and measure zstage to calibrate pfsConversion.
        this.setPFSOffset(0);
        try {
            Globals.core().fullFocus();
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
        this.setAutoFocusEnabled(true);
        SimpleRegression regression = new SimpleRegression(false);
        for (int offset=0; offset<MAX_PFS_OFFSET; offset+=10) { //TODO see how well this works and reduce the number of iterations to speed it up. maybe we don't need to go to max offset.
            this.setPFSOffset(offset);
            regression.addData(offset, this.getPosUm());
        }
        this.pfsConversionSlope = regression.getSlope();
        double r2 = regression.getRSquare();
        Globals.mm().logs().logMessage(String.format("PWSPlugin: Nikon zStage calibrated. Slope %f, R2 %f", pfsConversionSlope, r2));
        calibrated = true;
    }
    
   /* //@Override
    protected double convertValueToUm(double val) throws MMDeviceException{
        if (this.getAutoFocusEnabled()) {
            return val * pfsConversionSlope;
        } else {
            return val;
        }
    }
    
    //@Override
    protected double convertUmToValue(double um) throws MMDeviceException {
        if (this.getAutoFocusEnabled()) {
            return um / pfsConversion;
        } else {
            return um;
        }     
    }*/
    
    public void setPosRelativeUm(double um) throws MMDeviceException {
        try {
            if (this.getAutoFocusEnabled()) {
                if (!calibrated) { this.calibrate(); }
                double val = um / pfsConversionSlope; //This can only work for relative moves.
                    Globals.core().setAutoFocusOffset(val); //TODO this doesn't work on PFS
            } else {
                Globals.core().setRelativePosition(devName, um); //TODO do we need to block here?
            }
        } catch (Exception e) {
            throw new MMDeviceException(e);
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
    
    @Subscribe
    public void focusChanged(PropertyChangedEvent evt) {
        
        try {
            if (evt.getProperty().equals("Position") && Globals.core().getDeviceName(evt.getDevice()).equals("TIPFSOffset")) {//TODO i made up these names
                if (!evt.getValue().equals("Locked")) {
                    String currentState = ""; // TODO
                    pcs.firePropertyChange("focusLock", oldState, currentState);
                    oldState = currentState;
                }
            }
        } catch (Exception e) {
            Globals.mm().logs().logError(e);
        }
    }
        
    public void addFocusLockListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener("focusLock", listener);
    }
}
