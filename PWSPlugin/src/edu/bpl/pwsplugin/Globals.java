/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import mmcorej.CMMCore;
import org.micromanager.Studio;

/**
 *
 * @author LCPWS3
 */
public class Globals {
    private static Globals instance = null;
    private Studio studio_ = null;
    private AcqManager acqMan_;
    private HWConfiguration config;
    private MMConfigAdapter mmAdapter;
    private PluginFrame frame;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private Globals() {}
    
    public static Globals instance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }
    
    public static void init(Studio studio) {
        instance().studio_ = studio;
        instance().mmAdapter = new MMConfigAdapter();
        instance().acqMan_ = new AcqManager();
        instance().frame = new PluginFrame();
    }
    
    public static void addPropertyChangeListener(PropertyChangeListener l) {
        instance().pcs.addPropertyChangeListener(l);
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener l) {
        instance().pcs.removePropertyChangeListener(l);
    }
            
    public static Studio mm() {
        return instance().studio_;
    }
    
    public static CMMCore core() {
        return instance().studio_.core();
    }
    
    public static AcqManager acqManager() {
        return instance().acqMan_;
    }
    
    public static PluginFrame frame() {
        return instance().frame;
    }
    
    public static void setHardwareConfigurationSettings(HWConfigurationSettings configg) {
        instance().config = new HWConfiguration(configg);
        instance().pcs.firePropertyChange("config", null, instance().config);
    }
    
    public static HWConfiguration getHardwareConfiguration() {
        return instance().config;
    }
    
    public static MMConfigAdapter getMMConfigAdapter() {
        return instance().mmAdapter;
    }

}