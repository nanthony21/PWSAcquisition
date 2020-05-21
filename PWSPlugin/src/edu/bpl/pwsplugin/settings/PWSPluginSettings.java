
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
    public HWConfigurationSettings hwConfiguration = new HWConfigurationSettings();
    public FluorSettings flSettings = new FluorSettings();
    public DynSettings dynSettings = new DynSettings();
    public PWSSettings pwsSettings = new PWSSettings();
    public String saveDir = "";
    public int cellNum = 1;

    public static PWSPluginSettings fromJsonString(String str) {
        return (PWSPluginSettings) JsonableParam.fromJson(str, PWSPluginSettings.class);
    }
}
