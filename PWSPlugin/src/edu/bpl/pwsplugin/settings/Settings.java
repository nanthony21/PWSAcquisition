
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.utils.UIBuildable;
import java.util.List;

/**
 *
 * @author nick
 */
public class Settings {
    public static class PWSSettings implements UIBuildable{
        public int wvStart;
        public int wvStop;
        public int wvStep;
        public double exposure;
        public boolean ttlTriggering;
        public boolean externalCamTriggering;
    }
    
    public static class DynSettings implements UIBuildable {
        public double exposure;
        public int wavelength;
        public int numFrames;
    }
    
    public static class FluorSettings implements UIBuildable {
        public double exposure;
        public String filterConfigName;
        public boolean useAltCamera;
        public String altCamName;
        public int tfWavelength;
    }
    
    public static class SysConfig implements UIBuildable {
     //TODO
        public String systemName;
        public List<CamSettings> cameras;
    }
    
    public static class CamSettings implements UIBuildable {
        public String name;
        public String linearityPolynomial; //DO we want to use a string for this?
        public int darkCounts;
        public boolean hasTunableFilter;
        public String tunableFilterName;
    }
}
