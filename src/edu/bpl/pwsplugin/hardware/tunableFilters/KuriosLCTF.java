
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class KuriosLCTF extends DefaultTunableFilter {
    
    public KuriosLCTF(TunableFilterSettings settings) throws Device.IDException {
        super(settings, "Wavelength");
    }
    
    @Override
    public boolean identify() {
        try {
            return Globals.core().getDeviceName(this.devName).equals("Kurios LCTF");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        if (!identify()) {
            errs.add("KuriosLCTF: Could not find device named " + this.devName + ".");
        }
        try {
            String prop = Globals.core().getProperty(devName, "Status");
            if (!prop.equals("Ready")) {
                errs.add(String.format("Kurios LCTF indicates that the device is `%s` rather than `Ready`, please wait.", prop));
            }
            prop = Globals.core().getProperty(devName, "Spectral Range");
            if (!prop.equals("Visible")) {
                errs.add(String.format("Kurios LCTF reports a spectral range of `%s` rather than `Visible`", prop));
            }
            prop = Globals.core().getProperty(devName, "Bandwidth");
            if (!prop.equals("Narrow")) {
                errs.add("Kurios LCTF `Bandwidth` must be set to `Narrow` bandwidth."); //TODO this was decided arbitrarily.
            }
            
            prop = Globals.core().getProperty(devName, "Sequence Bandwidth");
            if (!prop.equals("Narrow")) {
                errs.add("Kurios LCTF `Sequencing Bandwidth` must be set to `Narrow` bandwidth."); //TODO this was decided arbitrarily.
            }
        } catch (Exception e) {
            Globals.mm().logs().logError(e);
            errs.add("Error in Kurios LCTF validation. Please see corelog file.");
        }
        return errs; 
    }
}
