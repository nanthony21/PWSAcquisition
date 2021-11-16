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
kurios still goes one item too far in sequencing. Only when external triggering isn't used. Device adapter: starts at 502 instead of 500
Add an optional timeout to pause step  so it can be used as a delay.
Multiple instances of the ZStage are created for each configuration.

3:
comments and tooltips. Fix "external TTL" tooltip


4:
Transmission imaging

Nikon:

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
import edu.bpl.pwsplugin.acquisitionsequencer.Sequencer;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFactoryManager;
import edu.bpl.pwsplugin.settings.PWSPluginSettings;
import edu.bpl.pwsplugin.settings.PWSSettingsConsts;
import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.micromanager.events.ShutdownCommencingEvent;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * This class implements the functionality that Micro-Manager needs in order to accept our code as a plugin.
 */
@Plugin(type = MenuPlugin.class)
public class PWSPlugin implements MenuPlugin, SciJavaPlugin {
   public final static String menuName = "PWS Acquisition";
   public final static String versionNumber = "0.6";

   private Studio studio_;
   private boolean initialized_ = false;

   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
      //This allows us to run cleanup when shutdown begins, see `closeRequested`
      studio.events().registerForEvents(this);
   }

   @Override
   public void onPluginSelected() { //This is fired when the user requests to open the plugin.
      if (!initialized_) {
         //In order for json serial/deserialization to work, each class must be
         //registered with Gson. Let's do that now to make sure.
         //They also register themselves when they are instantiated but that may not happen in time.
         PWSSettingsConsts.registerGson();
         PWSPluginSettings.registerGsonType();
         Sequencer sequencer = new Sequencer();

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
   public void closeRequested(
         ShutdownCommencingEvent sce) { //This is fired when micro-manager indicates that it is closing.
      if (Globals.frame() != null) {
         if (!sce.getIsCancelled()) {
            Globals.frame().dispose(); //This should also cause settings to be saved.
         }
      }
   }

   //API
   public Globals api() {
      return Globals.instance();
   }
}
