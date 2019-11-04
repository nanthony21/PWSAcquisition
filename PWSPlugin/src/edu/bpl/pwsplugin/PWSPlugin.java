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

    public static String menuName = "PWS Acquisition";
    public static String tooltipDescription = "Hyperspectral Imaging";
    public static String versionNumber = "0.3";
    public static String copyright = "Backman Photonics Lab";
    
    //Strings which are used to save and load settings for the plugin.
    public static class Settings {
        public static String wv = "wv"; //The array of wavelengths to image at.
        public static String start = "start"; //The first wavelength to image
        public static String stop = "stop"; //The last wavelength to image
        public static String step = "step"; //The interval between each wavelength
        public static String darkCounts = "darkCounts"; // The darkcounts of the PWS camera. darkcounts-per-pixel.
        public static String linearityPoly = "linearityPoly"; //A polynomial that linearizes the counts of the camera to intensity. This is measured by measuring a sample over a range of exposure times.
        public static String sequence = "sequence"; //Whether or not to use hardware triggering between the camera and the spectral filter. Speeds up acquisition but makes the software more complex sometimes.
        public static String externalTrigger = "externalTrigger"; //Whether the camera exposures should be triggered by some external hardware. This feature is hardly supported.
        public static String savePath  = "savepath"; // The path to save to
        public static String cellNum  = "cellNum"; //The number of the acquisiiton folder.
        public static String filterLabel = "filterLabel"; //The micromanager device name for the spectral filter.
        public static String systemName = "systemName"; //The identifying name for this system.
        public static String exposure = "pwsExposure"; //The exposure time for the camera for PWS
        public static String dynExposure = "dynExposure"; //The exposure time for dynamics.
        public static String dynWavelength = "dynWavelength"; //The wavelength that the LCTF should be set to for dynamics.
        public static String dynNumFrames = "dynNumFrames"; //The number of frames to acquire for dynamics.
        public static String flFilterBlock = "flFilterBlock"; //The filter block to switch to for fluorescence images.
        public static String flExposure = "flExposure"; //The exposure time to use for fluorescence.
        public static String flWavelength = "flWavelength"; //The wavelength to set the filter to when imaging fluorescence through the spectral filter.
        public static String altCamFl = "altCamFl"; //Whether or not we are imaging fluorescence using the same camera as for PWS or using a separate camera.
        public static String flAltCamName = "flAltCamName";  //If we are using another camera what is it's name in our "Camera" config group.
        public static String camTransform = "camTransform"; //An affine transform between the two cameras.
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
            initialized_ = false;
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
