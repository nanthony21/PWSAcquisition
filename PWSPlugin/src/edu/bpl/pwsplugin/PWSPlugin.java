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

//Must Do:
//Generate camera affine transform
//TODO test test test
//Optional:
//Add display album for fluorescence. Make fluorescence acq engine support multiple channels.
//Step types:
//GoTo step
//TODO XYZ grid (can be used for z-stacks)
//Implement builder pattern for settings.
//Add sequence "treepath" to the status class.
//Expand usage of metadata class.


import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.CamSettings;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.HWConfigurationSettings;
import edu.bpl.pwsplugin.settings.IlluminatorSettings;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
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
            JsonableParam.registerClass(AcquireCellSettings.class);
            Consts.registerGson();
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
    
    public void dispose() { //Close the frame.
        Globals.frame().dispose();
    }
}
