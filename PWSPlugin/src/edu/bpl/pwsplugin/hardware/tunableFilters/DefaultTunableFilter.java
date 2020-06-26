
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import mmcorej.StrVector;

/**
 *
 * @author N2-LiveCell
 */
public abstract class DefaultTunableFilter extends TunableFilter{
    //Provides a default implementation for the abstract methods of `TunableFilter` class.
    //Based on a device name and wavelength property name this class implements methods by calling the corresponding MMCore functions.
    protected String devName;
    private TunableFilterSettings _settings;
    private String wvProp;
    
    public DefaultTunableFilter(TunableFilterSettings settings, String wvPropertyLabel) {
        _settings = settings;
        this.devName = settings.name;
        this.wvProp = wvPropertyLabel;
    }
    
    @Override
    public void setWavelength(int wavelength) throws MMDeviceException {
        try {
            Globals.core().setProperty(devName, wvProp, String.valueOf(wavelength));
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    
    @Override
    public int getWavelength() throws MMDeviceException{ 
        try {
            int wv = (int) Math.round(Double.valueOf(Globals.core().getProperty(devName, wvProp)));
            return wv;
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean supportsSequencing() { return true; } 
    
    @Override
    public int getMaxSequenceLength() throws MMDeviceException {
        try {
            return Globals.core().getPropertySequenceMaxLength(devName, wvProp);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public void loadSequence(int[] wavelengthSequence) throws MMDeviceException {
        StrVector strv = new StrVector();
        for (int i = 0; i < wavelengthSequence.length; i++) {   //Convert wv from int to string for sending to the device.
            strv.add(String.valueOf(wavelengthSequence[i]));
        }
        try {
            Globals.mm().core().loadPropertySequence(devName, wvProp, strv);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public void startSequence() throws MMDeviceException {
        try {
            Globals.core().startPropertySequence(devName, wvProp);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public void stopSequence() throws MMDeviceException {
        try {
            Globals.core().stopPropertySequence(devName, wvProp);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public boolean isBusy() throws MMDeviceException {
        try {
            return Globals.core().deviceBusy(devName);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public double getDelayMs() throws MMDeviceException {
        try {
            return Globals.core().getDeviceDelayMs(devName);
        } catch (Exception e) {
            throw new MMDeviceException(e);
        }
    }
    
    @Override
    public TunableFilterSettings getSettings() {
        return _settings;
    }
    
    @Override
    public void initialize() {}//Not sure what to do here
    
    @Override
    public void activate() {}//Not sure what to do here
}
