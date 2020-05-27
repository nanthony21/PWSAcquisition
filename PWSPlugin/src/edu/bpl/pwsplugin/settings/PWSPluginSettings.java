
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.utils.JsonableParam;


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
    public ContainerStep sequenceRoot = (ContainerStep) Consts.getFactory(Consts.Type.ROOT).createStep();
    public String saveDir = "";
    public int cellNum = 1;

    public static PWSPluginSettings fromJsonString(String str) {
        return (PWSPluginSettings) JsonableParam.fromJson(str, PWSPluginSettings.class);
    }
}
