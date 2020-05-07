/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import java.nio.file.Path;

/**
 *
 * @author nick
 */
public class AcquireDynamics implements Step {
    //This doesn't increment the cell number because for all we know we still want to save other acquisition types to the same cell number.
    Path directory;
    AcquisitionManager acqMan;
    public AcquireDynamics(Path directoryName, AcquisitionManager acqMan) {
        //Acquires and saves a PWS cube returns the number of acquisitions saved: 1.
        directory = directoryName;
        this.acqMan = acqMan;
    }
    
    @Override
    public Integer applyThrows(Integer cellNum) {
        this.acqMan.setCellNum(cellNum);
        this.acqMan.setSavePath(this.directory.toString());
        this.acqMan.acquireDynamics();
        return 1; 
    }   
}
