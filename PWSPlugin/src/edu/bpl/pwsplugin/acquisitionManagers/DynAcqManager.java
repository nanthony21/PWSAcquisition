/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.PWSAlbum;
import org.micromanager.Studio;

/**
 *
 * @author N2-LiveCell
 */
public class DynAcqManager {
    private Studio studio_;
    double exposure_;
    String filtLabel_;
    int wavelength_;
    
    public DynAcqManager(Studio studio){
        studio_ = studio;
    }
    
    public void setSequenceSettings(double exposure, String filtLabel, int wavelength) {
        exposure_ = exposure;
        filtLabel_ = filtLabel;
        wavelength_ = wavelength;
    }
    
    public void run(int cellNum, String savePath, PWSAlbum album) {
        
    }
}
