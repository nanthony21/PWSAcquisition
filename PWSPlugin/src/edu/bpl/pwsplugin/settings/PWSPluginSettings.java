
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.RootStep;
import edu.bpl.pwsplugin.utils.JsonableParam;


/**
 *
 * @author nick
 */
public class PWSPluginSettings extends JsonableParam {
    //This is just a container for all the other settings. this is the main object that gets
    //passed around, saved, loaded, etc.
    public HWConfigurationSettings hwConfiguration = new HWConfigurationSettings();
    public AcquireCellSettings acquisitionSettings = new AcquireCellSettings();
    public RootStep sequenceRoot = (RootStep) Consts.getFactory(Consts.Type.ROOT).createStep();
    public String saveDir = "";
    public int cellNum = 1;

    public static PWSPluginSettings fromJsonString(String str) {
        return (PWSPluginSettings) JsonableParam.fromJson(str, PWSPluginSettings.class);
    }
}
