/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import mmcorej.CMMCore;
import org.micromanager.Studio;

/**
 *
 * @author LCPWS3
 */
public class Globals {
    private static Studio studio_ = null;
    private static AcqManager acqMan_ = null;
    
    public static void init(Studio studio) {
        studio_ = studio;
        acqMan_ = new AcqManager(); //Very important that this is instantiated after studio. this is probably bade design actually.
    }
            
    public static Studio mm() {
        return studio_;
    }
    
    public static CMMCore core() {
        return studio_.core();
    }
    
    public static AcqManager acqManager() {
        return acqMan_;
    }
    
}
