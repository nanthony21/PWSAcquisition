///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin for Micro-Manager
//
//-----------------------------------------------------------------------------
//
// AUTHOR:      Nick Anthony 2020
//
// COPYRIGHT:    Northwestern University, Evanston, IL 2020
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
TODO
1:
LCPWS2 turned off TTL when using quick acquire.
Remove validate at system start.
kurios still goes one item too far in sequencing. Only when external triggering isn't used. Device adapter: starts at 502 instead of 500
Same is happening to varispec LCTF!!


ZStage autofocus routine bumps the sample? It seems like it shouldn't though. Maybe I'm wrong.
test software autofocus
test focus lock in realistic situation, ethanol cells and weird dishes.
Remove objective config group. -> Allow selecting "None" for objective config group.


2:
Add checkbox to enable sharpness measurement. Add label when no ROI is drawn.
auto-scan z for focus sharpness.
Make nikon recalibrate when objective changes. (fix config group event). Make objective a `Device`?
remove "configuration" from simple PWS/ dynamics view.

3:
comments and tooltips. Fix "external TTL" tooltip
clear autoexposure display when re-running


4:
Transmission imaging

Nikon:
    Sometimes TI2 PFS becomes disabled during calibration.

Dialogs:
    Autofocus: open timed dialog for if retry should happen
    Same for focus lock and it's callback

Some other day:
    Focus lock snaps a single image and tests sharpness
    Add autoexposure to sequencer
    STORM imaging
    Add a cell selection step (AI type stuff)
    Z-Drive controller UI
*/

package edu.bpl.pwsplugin;

import com.google.common.eventbus.Subscribe;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettingsConsts;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.micromanager.Studio;
import org.micromanager.MenuPlugin;
import org.micromanager.events.ShutdownCommencingEvent;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.plugin.Plugin;
   
@Plugin(type = MenuPlugin.class)
public class PWSPlugin implements MenuPlugin, SciJavaPlugin {
    //This class implements the functionality that Micro-Manager needs in order to accept our code as a plugin.

    public static String menuName = "PWS Acquisition";
    public static String versionNumber = "0.5";
        
    private Studio studio_;  
    private boolean initialized_ = false;
    
    @Override
    public void setContext(Studio studio) {
        studio_ = studio;
        studio.events().registerForEvents(this); //This allows us to run cleanup when shutdown begins, see `closeRequested`
        Timer t = new Timer(1000, null);
        t.addActionListener((evt) -> {
            try {
                if (studio_.app().getMainWindow() != null) {
                    this.onPluginSelected();
                    t.stop();
                }
            } catch (Exception e) {
                System.out.print(e);
            }

        });
        t.setRepeats(true);
        t.start();
    } 
    
    @Override
    public void onPluginSelected() { //This is fired when the user requests to open the plugin.
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
    public void closeRequested( ShutdownCommencingEvent sce){ //This is fired when micro-manager indicates that it is closing.
        if (Globals.frame() != null) {
            if (!sce.getIsCancelled()) {
                Globals.frame().dispose(); //This should also cause settings to be saved.
            }
        }
   }
    
    public void dispose() { //Close the frame.
        Globals.frame().dispose();
    }
}
