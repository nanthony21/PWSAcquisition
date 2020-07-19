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

/*
1:
test software autofocus
test focus lock in realistic situation, ethanol cells and weird dishes.

2:
Make nikon recalibrate when objective changes. (fix config group event). Make objective a `Device`?

3:
Focus lock snaps a single image and tests sharpness
PFS set to current position function.
comments and tooltips

4:
Transmission imaging
kurios still goes one item too far in sequencing. Only when external triggering isn't used. Device adapter: starts at 502 instead of 500

Add a description field to initialization
Allow user to check settings and stuff while acquisition is running, just block `action buttons`.
Positions not recoverable from metadata.
Autoshutter step
Sequencer mark cancel button when waiting for cancel

Nikon:
    Sometimes TI2 PFS becomes disabled during calibration.


Some other day:
    Add autoexposure to sequencer
    STORM imaging
    Add a cell selection step (AI type stuff)
    Z-Drive controller UI


*/

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
    public static String versionNumber = "0.5";
        
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
