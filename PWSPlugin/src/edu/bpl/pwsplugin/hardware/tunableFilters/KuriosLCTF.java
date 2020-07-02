
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
            return Globals.core().getDeviceName(this.devName).equals("TODO");
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
        //TODO check if warmed up. check bandwidth mode
        return errs; 
    }
}
