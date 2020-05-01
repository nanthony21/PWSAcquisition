/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    
    public void init(Studio studio) {
        studio_ = studio;
        mmAdapter = new MMConfigAdapter();
        acqMan_ = new AcqManager();
        frame = new PluginFrame();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.pcs.removePropertyChangeListener(l);
    }
            
    public Studio mm() {
        return studio_;
    }
    
    public CMMCore core() {
        return studio_.core();
    }
    
    public AcqManager acqManager() {
        return acqMan_;
    }
    
    public PluginFrame frame() {
        return frame;
    }
    
    public void setHardwareConfigurationSettings(HWConfigurationSettings configg) {
        config = new HWConfiguration(configg);
        pcs.firePropertyChange("config", null, config);
    }
    
    public HWConfiguration getHardwareConfiguration() {
        return config;
    }
    
    public MMConfigAdapter getMMConfigAdapter() {
        return mmAdapter;
    }

}