/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.nio.file.Path;

/**
 *
 * @author nick
 */
public class AcquirePWS implements Step{
    //This doesn't increment the cell number because for all we know we still want to save other acquisition types to the same cell number.
    Path directory;
    AcquisitionManager acqMan;
    PWSSettings settings;
    
    public AcquirePWS(Path directoryName, AcquisitionManager acqMan, PWSSettings settings) {
        //Acquires and saves a PWS cube returns the number of acquisitions saved: 1.
        directory = directoryName;
        this.acqMan = acqMan;
        this.settings = settings;
    }
    
    @Override
    public Integer applyThrows(Integer cellNum) throws Exception{
        this.acqMan.setCellNum(cellNum);
        this.acqMan.setSavePath(this.directory.toString());
        this.acqMan.setPWSSettings(settings);
        this.acqMan.acquirePWS();
        return 1; 
    }   
}
