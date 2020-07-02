
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class KuriosLCTF extends DefaultTunableFilter {
    
    public KuriosLCTF(TunableFilterSettings settings) {
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
        } catch (Exception e) {
            Globals.mm().logs().logError(e);
            errs.add("Error in Kurios LCTF validation. Please see corelog file.");
        }
        //TODO What do we do about checking bandwidth?
        return errs; 
    }
}
