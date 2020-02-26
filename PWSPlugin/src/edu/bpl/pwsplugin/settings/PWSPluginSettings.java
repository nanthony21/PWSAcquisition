
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.hardware.cameras.Camera;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.tunableFilters.TunableFilter;
import edu.bpl.pwsplugin.utils.JsonableParam;
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
    public static class PWSSettings extends JsonableParam{
        public int wvStart;
        public int wvStop;
        public int wvStep;
        public double exposure;
        public boolean ttlTriggering;
        public boolean externalCamTriggering;
        
        public int[] getWavelengthArray() {
            int numWvs = java.lang.Math.abs(wvStart - wvStop) / wvStep + 1;
            int[] wvs = new int[numWvs];
            int index = 0;
            for (int i = wvStart; i <= wvStop; i += wvStep) {
                wvs[index] = i;
                index++;
            }   
            return wvs;
        }
    }
    
    public static class DynSettings extends JsonableParam {
        public double exposure;
        public int wavelength;
        public int numFrames;
    }
    
    public static class FluorSettings extends JsonableParam {
        public double exposure;
        public String filterConfigName;
        public boolean useAltCamera;
        public String altCamName;
        public int tfWavelength;
    }
    
    public static class HWConfiguration extends JsonableParam {
        public String systemName;
        public List<ImagingConfigurationSettings> configs;
        
        public static class CamSettings extends JsonableParam {
            public String name;
            public Camera.Types camType;
            public List<Double> linearityPolynomial;
            public int darkCounts;
            public double[] affineTransform; //A 2x3 affine transformation matrix specifying how coordinates in one camera translate to coordinates in another camera. For simplicity we store this array as a 1d array of length 6
        }
        
        public static class TunableFilterSettings extends JsonableParam {
            public String name;
            public TunableFilter.Types filterType;
        }
        
        public static class ImagingConfigurationSettings extends JsonableParam {
            public String name;
            public ImagingConfiguration.Types configType;
            public CamSettings camSettings;
            public TunableFilterSettings filtSettings;
        }
    }
}
