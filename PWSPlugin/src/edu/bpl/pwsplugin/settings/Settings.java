
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;
import edu.bpl.pwsplugin.utils.UIBuildable;
import java.util.List;

/**
 *
 * @author nick
 */
public class Settings {
    public static class PWSSettings extends JsonableParam implements UIBuildable{
        public int wvStart;
        public int wvStop;
        public int wvStep;
        public double exposure;
        public boolean ttlTriggering;
        public boolean externalCamTriggering;
    }
    
    public static class DynSettings extends JsonableParam implements UIBuildable {
        public double exposure;
        public int wavelength;
        public int numFrames;
    }
    
    public static class FluorSettings extends JsonableParam implements UIBuildable {
        public double exposure;
        public String filterConfigName;
        public boolean useAltCamera;
        public String altCamName;
        public int tfWavelength;
    }
    
    public static class HWConfiguration extends JsonableParam implements UIBuildable {
     //TODO
        public String systemName;
        public List<CamSettings> cameras;
    }
    
    public static class CamSettings extends JsonableParam implements UIBuildable {
        public String name;
        public String linearityPolynomial; //DO we want to use a string for this?
        public int darkCounts;
        public boolean hasTunableFilter;
        public String tunableFilterName;
    }
}
