
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.TunableFilterSettings;
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
    public void setWavelength(int wavelength) throws Exception {
        Globals.instance().core().setProperty(devName, wvProp, String.valueOf(wavelength));
    }
    
    
    @Override
    public int getWavelength() throws Exception{ 
        int wv = (int) Math.round(Double.valueOf(Globals.instance().core().getProperty(devName, wvProp)));
        return wv;
    }
    
    @Override
    public boolean supportsSequencing() { return true; } 
    
    @Override
    public int getMaxSequenceLength() throws Exception {
        return Globals.instance().core().getPropertySequenceMaxLength(devName, wvProp);
    }
    
    @Override
    public void loadSequence(int[] wavelengthSequence) throws Exception {
        StrVector strv = new StrVector();
        for (int i = 0; i < wavelengthSequence.length; i++) {   //Convert wv from int to string for sending to the device.
            strv.add(String.valueOf(wavelengthSequence[i]));
        }
        Globals.instance().mm().core().loadPropertySequence(devName, wvProp, strv);
    }
    
    @Override
    public void startSequence() throws Exception {
        Globals.instance().core().startPropertySequence(devName, wvProp);
    }
    
    @Override
    public void stopSequence() throws Exception {
        Globals.instance().core().stopPropertySequence(devName, wvProp);
    }
    
    @Override
    public boolean isBusy() throws Exception {
        return Globals.instance().core().deviceBusy(devName);
    }
    
    @Override
    public double getDelayMs() throws Exception {
        return Globals.instance().core().getDeviceDelayMs(devName);
    }
    
    @Override
    public TunableFilterSettings getSettings() {
        return _settings;
    }
}
