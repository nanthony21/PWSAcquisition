
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
    public HWConfigurationSettings hwConfiguration;
    public FluorSettings flSettings;
    public DynSettings dynSettings;
    public PWSSettings pwsSettings;
    public String saveDir;
    public int cellNum;

    public static PWSPluginSettings fromJsonString(String str) {
        return (PWSPluginSettings) JsonableParam.fromJsonString(str, PWSPluginSettings.class);
    }
}
