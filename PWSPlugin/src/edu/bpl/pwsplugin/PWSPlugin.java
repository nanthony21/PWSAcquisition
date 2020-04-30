package edu.bpl.pwsplugin;
///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2019
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2019
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.List;
import org.micromanager.Studio;
import org.micromanager.MenuPlugin;
import org.micromanager.events.ShutdownCommencingEvent;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.plugin.Plugin;
   
@Plugin(type = MenuPlugin.class)
public class PWSPlugin implements MenuPlugin, SciJavaPlugin {

    public static String menuName = "PWS Acquisition";
    public static String versionNumber = "0.4";
        
    private Studio studio_;  
    private PluginFrame frame_;
    private boolean initialized_ = false;
    
    @Override
    public void setContext(Studio studio) {
        studio_ = studio;
    } 
    
    @Override
    public void onPluginSelected() {
        if (!initialized_) {
            //In order for json serial/deserialization to work, each class must be 
            //registered with Gson. Let's do that now to make sure.
            //They also register themselves when they are instantiated but that may not happen in time.
            JsonableParam.registerClass(PWSPluginSettings.FluorSettings.class);
            JsonableParam.registerClass(PWSPluginSettings.PWSSettings.class);
            JsonableParam.registerClass(PWSPluginSettings.DynSettings.class);
            JsonableParam.registerClass(PWSPluginSettings.HWConfiguration.class);
            JsonableParam.registerClass(PWSPluginSettings.HWConfiguration.CamSettings.class);
            JsonableParam.registerClass(PWSPluginSettings.class);
            
            Globals.init(studio_);
            frame_ = new PluginFrame();
            
            initialized_ = true;
        }
        frame_.setVisible(true);
    }
    
    @Override
    public String getSubMenu() {
        return "Acquisition Tools";
    }

    @Override
    public String getHelpText() {
        return "Partial Wave Spectroscopic Microscopy";
    }

    @Override
    public String getName() {
        return menuName;
    }

    @Override
    public String getVersion() {
        return versionNumber;
    }

    @Override
    public String getCopyright() {
        return "Backman Biophotonics Lab";
    }
    
    @Subscribe
    public void closeRequested( ShutdownCommencingEvent sce){
      if (frame_ != null) {
         if (!sce.getIsCancelled()) {
            frame_.dispose();
         }
      }
   }
    

    
    //API
    public void setSavePath(String savepath) {
        frame_.setSavePath(savepath);
    }
    
    public void setCellNumber(int cellNum) {
        frame_.setCellNumber(cellNum);
    }
    
    public void setPWSExposure(double exposureMs) {
        frame_.setPWSExposure(exposureMs);
    }

    public void acquirePWS() {
        frame_.acquirePws();
    }
    
    public void acquireDynamics() {
        frame_.acquireDynamics();
    }
    
    public void acquireFluorescence() {
        frame_.acquireFluorescence();
    }
    
    public void setDynamicsExposure(double exposureMs) {
        frame_.setDynamicsExposure(exposureMs);
    }
    
    public void setFluorescenceExposure(double exposureMs) {
        frame_.setFluorescenceExposure(exposureMs);
    }
    
    public void setFluorescenceFilter(String filterBlockName) {
        frame_.setFluorescenceFilter(filterBlockName);
    }
    
    public List<String> getFluorescenceFilterNames() {
        return frame_.getFluorescenceFilterNames();
    }
    
    public boolean isAcquisitionRunning() {
        return Globals.acqManager().isAcquisitionRunning();
    }
    
    public String getFilterName() {
        return frame_.getFilterName();
    }
    
    public void setFluorescenceEmissionWavelength(int wv) {
        frame_.setFluorescenceEmissionWavelength(wv);
    }
    
    public void dispose() { //Close the frame.
        frame_.dispose();
    }
}
