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

//TODO add z offset for fluorescence, add sequencing, file overwriting.

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.UI.PluginFrame;
import edu.bpl.pwsplugin.settings.CamSettings;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.IlluminatorSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
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
            JsonableParam.registerClass(FluorSettings.class);
            JsonableParam.registerClass(PWSSettings.class);
            JsonableParam.registerClass(DynSettings.class);
            JsonableParam.registerClass(HWConfigurationSettings.class);
            JsonableParam.registerClass(CamSettings.class);
            JsonableParam.registerClass(IlluminatorSettings.class);
            JsonableParam.registerClass(PWSPluginSettings.class);
            edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings.registerWithGSON();
            Globals.init(studio_);       
            initialized_ = true;
        }
        Globals.frame().setVisible(true);
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
      if (Globals.frame() != null) {
         if (!sce.getIsCancelled()) {
            Globals.frame().dispose();
         }
      }
   }
    

    
    //API
    public void setSavePath(String savepath) {
        Globals.frame().setSavePath(savepath);
    }
    
    public void setCellNumber(int cellNum) {
        Globals.frame().setCellNumber(cellNum);
    }
    
    public void setPWSExposure(double exposureMs) {
        Globals.frame().setPWSExposure(exposureMs);
    }

    public void acquirePWS() {
        Globals.frame().acquirePws();
    }
    
    public void acquireDynamics() {
        Globals.frame().acquireDynamics();
    }
    
    public void acquireFluorescence() {
        Globals.frame().acquireFluorescence();
    }
    
    public void setDynamicsExposure(double exposureMs) {
        Globals.frame().setDynamicsExposure(exposureMs);
    }
    
    public void setFluorescenceExposure(double exposureMs) {
        Globals.frame().setFluorescenceExposure(exposureMs);
    }
    
    public void setFluorescenceFilter(String filterBlockName) {
        Globals.frame().setFluorescenceFilter(filterBlockName);
    }
    
    public List<String> getFluorescenceFilterNames() {
        return Globals.frame().getFluorescenceFilterNames();
    }
    
    public boolean isAcquisitionRunning() {
        return Globals.acqManager().isAcquisitionRunning();
    }
    
    public String getFilterName() {
        return Globals.frame().getFilterName();
    }
    
    public void setFluorescenceEmissionWavelength(int wv) {
        Globals.frame().setFluorescenceEmissionWavelength(wv);
    }
    
    public void dispose() { //Close the frame.
        Globals.frame().dispose();
    }
}
