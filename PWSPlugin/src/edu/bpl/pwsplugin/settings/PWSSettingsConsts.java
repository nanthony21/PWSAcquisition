package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author nick
 */
public class PWSSettingsConsts {
    /* this class contains constant information that is relevant to the settings
    
    */
    public static void registerGson() {
        /* this convenient function provides a single function to register all settings with GSON. make sure to call this somewhere during initialization.
        
        */
        JsonableParam.registerClass(FluorSettings.class);
        JsonableParam.registerClass(PWSSettings.class);
        JsonableParam.registerClass(DynSettings.class);
        JsonableParam.registerClass(HWConfigurationSettings.class);
        JsonableParam.registerClass(CamSettings.class);
        JsonableParam.registerClass(IlluminatorSettings.class);
        JsonableParam.registerClass(TunableFilterSettings.class);
        JsonableParam.registerClass(ImagingConfigurationSettings.class);
        JsonableParam.registerClass(PWSPluginSettings.class);
        JsonableParam.registerClass(AcquireCellSettings.class);
    }
    
    public enum Systems {
        LCPWS1,
        LCPWS2,
        LCPWS3,
        STORM;
    }
}
