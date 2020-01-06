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
import java.util.Vector;
import org.micromanager.Studio;
import org.micromanager.MenuPlugin;
import org.micromanager.events.ShutdownCommencingEvent;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.plugin.Plugin;
   
@Plugin(type = MenuPlugin.class)
public class PWSPlugin implements MenuPlugin, SciJavaPlugin {

    public static String menuName = "PWSAcquisition";
    public static String tooltipDescription = "Hyperspectral Imaging";
    public static String versionNumber = "0.2";
    public static String copyright = "Backman Photonics Lab";
    
    //Strings which are used to save and load settings for the plugin.
    public static class Settings {
        public static String wv = "wv";
        public static String start = "start";
        public static String stop = "stop";
        public static String step = "step";
        public static String darkCounts = "darkCounts";
        public static String linearityPoly = "linearityPoly";
        public static String sequence = "sequence";
        public static String externalTrigger = "externalTrigger";
        public static String savePath  = "savepath";
        public static String cellNum  = "cellNum";
        public static String filterLabel = "filterLabel";
        public static String systemName = "systemName";
        public static String exposure = "pwsExposure";
        public static String dynExposure = "dynExposure";
        public static String dynWavelength = "dynWavelength";
        public static String dynNumFrames = "dynNumFrames";
        public static String flFilterBlock = "flFilterBlock";
        public static String flExposure = "flExposure";
        public static String flWavelength = "flWavelength";
    }
        
    private Studio studio_;  
    private PWSFrame frame_;
    private AcqManager manager_; 
    private boolean initialized_ = false;
    
    @Override
    public void setContext(Studio studio) {
        studio_ = studio;
    } 
    
    @Override
    public void onPluginSelected() {
        if (!initialized_) {
            Globals.init(studio_);
            manager_ = new AcqManager();
            frame_ = new PWSFrame(manager_);
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
        return tooltipDescription;
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
        return copyright;
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
        frame_.acquirePWS();
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
    
    public Vector<String> getFluorescenceFilterNames() {
        return frame_.getFluorescenceFilterNames();
    }
    
    public boolean isAcquisitionRunning() {
        return manager_.isAcquisitionRunning();
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
