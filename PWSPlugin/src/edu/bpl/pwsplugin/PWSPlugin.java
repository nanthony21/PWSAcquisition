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
//Software autofocus log step?
//Add sequence logging.
//Transmission imaging
//STORM imaging

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettingsConsts;
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
            PWSSettingsConsts.registerGson();
            SequencerConsts.registerGson();
            Step.registerGsonType();
            PWSPluginSettings.registerGsonType();
            Globals.init(studio_);       
            initialized_ = true;
        }
        Globals.getMMConfigAdapter().refresh(); //Checks for the presence of specific config groups. etc.
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
