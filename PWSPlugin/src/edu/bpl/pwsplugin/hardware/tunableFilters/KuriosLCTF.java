
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author N2-LiveCell
 */
public class KuriosLCTF extends DefaultTunableFilter {
    String devName = "kuriosLCTF";
    final String wvProp = "Wavelength";
    
    public KuriosLCTF() {
        super("kuriosLCTF", "Wavelength");
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
