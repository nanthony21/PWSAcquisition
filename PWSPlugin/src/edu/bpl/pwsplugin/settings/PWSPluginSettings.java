
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;
import edu.bpl.pwsplugin.utils.UIBuildable;
import java.util.List;

/**
 *
 * @author nick
 */
public class PWSPluginSettings extends JsonableParam {
    //This is just a container for all the other settings. this is the main object that gets
    //passed around, saved, loaded, etc.
    public HWConfiguration hwConfiguration;
    public FluorSettings flSettings;
    public DynSettings dynSettings;
    public PWSSettings pwsSettings;
    public String saveDir;
    public int cellNum;

    public static PWSPluginSettings fromJsonString(String str) {
        return (PWSPluginSettings) JsonableParam.fromJsonString(str, PWSPluginSettings.class);
    }
    
    //Make sure that everything here that extends jsonableparam gets registered on startup in the plugin class.
    public static class PWSSettings extends JsonableParam implements UIBuildable{
        public int wvStart;
        public int wvStop;
        public int wvStep;
        public double exposure;
        public boolean ttlTriggering;
        public boolean externalCamTriggering;
        
        public int[] getWavelengthArray() {
            
        }
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
        public boolean autoFilterSwitching;
        
        public static class CamSettings extends JsonableParam implements UIBuildable {
            public String name;
            public String linearityPolynomial; //TODO  do we want to use a string for this?
            public int darkCounts;
            public boolean hasTunableFilter;
            public String tunableFilterName;
            public double[] affineTransform; //A 2x3 affine transformation matrix specifying how coordinates in one camera translate to coordinates in another camera. For simplicity we store this array as a 1d array of length 6
        }
    }
}
