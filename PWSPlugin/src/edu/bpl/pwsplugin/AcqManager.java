/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import org.micromanager.Studio;

/**
 *
 * @author N2-LiveCell
 */


public class AcqManager {
    private PWSAcqManager pwsManager_;
    private DynAcqManager dynManager_;
    private Studio studio_;
    
    public AcqManager(Studio studio) {
        studio_ = studio;
        pwsManager_ = new PWSAcqManager(studio_);
        dynManager_ = new DynAcqManager(studio_);
    }
    
    public void acquirePWS() {
        pwsManager_.run();
    }
    
    public void acquireDynamics() {
        dynManager_.run();
    }
}
