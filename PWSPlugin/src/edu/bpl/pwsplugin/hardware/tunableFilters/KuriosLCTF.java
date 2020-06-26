
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.ArrayList;
import java.util.Arrays;
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
    public List<String> validate() {
        List<String> errs = new ArrayList<>();
        List<String> devs = Arrays.asList(Globals.core().getLoadedDevices().toArray());
        if (!devs.contains(this.devName)) {
            errs.add("KuriosLCTF: Could not find device named " + this.devName + ".");
        }
        return errs; 
    }
}
