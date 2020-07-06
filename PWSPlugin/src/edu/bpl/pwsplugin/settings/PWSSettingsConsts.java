package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import edu.bpl.pwsplugin.hardware.settings.CamSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nick
 */
public class PWSSettingsConsts {
    public static void registerGson() {
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
