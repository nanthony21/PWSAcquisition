/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.util.Arrays;
import java.util.List;
import mmcorej.CMMCore;
import mmcorej.DeviceType;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author LCPWS3
 */
public class Globals {
    private static Studio studio_ = null;
    private static AcqManager acqMan_;
    private static HWConfiguration config;
    private static MMConfigAdapter mmAdapter;
    private static PluginFrame frame;
    
    public static void init(Studio studio) {
        studio_ = studio;
        mmAdapter = new MMConfigAdapter();
        acqMan_ = new AcqManager();
        setHardwareConfigurationSettings(new HWConfigurationSettings()); //Very important that this is instantiated after studio. this is probably bad design actually.
        frame = new PluginFrame();
    }
            
    public static Studio mm() {
        return studio_;
    }
    
    public static CMMCore core() {
        return studio_.core();
    }
    
    public static AcqManager acqManager() {
        return acqMan_;
    }
    
    public static PluginFrame frame() {
        return frame;
    }
    
    public static void setHardwareConfigurationSettings(HWConfigurationSettings configg) {
        config = new HWConfiguration(configg);
    }
    
    public static HWConfiguration getHardwareConfiguration() {
        return config;
    }
    
    public static MMConfigAdapter getMMConfigAdapter() {
        return mmAdapter;
    }

}