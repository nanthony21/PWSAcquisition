package edu.bpl.pwsplugin;

/*
 * Copyright © 2009 – 2013, Marine Biological Laboratory
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of 
 * the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of any organization.
 * 
 * PWS Acquisition Plugin
 * @author Nick Anthony (nicholas.anthony@northwestern.edu)
 * Backman Photonics Lab, Northwestern University, Evanston, IL.
 * 
 * 
 * Updated for Micromanager 2.0 by Nick Anthony (nicholas.anthony@northwestern.edu)
 * Backman Photonics Lab, Northwestern University, Evanston, IL.
 */

import com.google.common.eventbus.Subscribe;
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
    
    public static String wvSetting = "wv";
    public static String startSetting = "start";
    public static String stopSetting = "stop";
    public static String stepSetting = "step";
    public static String darkCountsSetting = "darkCounts";
    public static String linearityPolySetting = "linearityPoly";
    public static String sequenceSetting = "sequence";
    public static String externalTriggerSetting = "externalTrigger";
    public static String savePathSetting  = "savepath";
    public static String cellNumSetting  = "cellNum";
    public static String filterLabelSetting = "filterLabel";
    public static String systemNameSetting = "systemName";
    public static String exposureSetting = "pwsExposure";
    public static String dynExposureSetting = "dynExposure";
    public static String dynWavelengthSetting = "dynWavelength";
    public static String dynNumFramesSetting = "dynNumFrames";
    
    private Studio studio_;  
    private PWSFrame frame_;
    private AcqManager manager_; 
    
    @Override
    public void setContext(Studio studio) {
        studio_ = studio;
    } 
    
    @Override
    public void onPluginSelected() {
        if (manager_ == null) {
            manager_ = new AcqManager(studio_);
        }
        if (frame_ == null) {
            frame_ = new PWSFrame(studio_, manager_);
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
    
    public void setDynamicsExposure(double exposureMs) {
        frame_.setDynamicsExposure(exposureMs);
    }
    
    public boolean isAcquisitionRunning() {
        return manager_.isAcquisitionRunning();
    }
    
    public String getFilterName() {
        return frame_.getFilterName();
    }
    
    public void dispose() { //Close the frame.
        frame_.dispose();
    }
}
