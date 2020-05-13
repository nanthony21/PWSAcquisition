/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.nio.file.Path;

/**
 *
 * @author nick
 */
public class AcquireFluorescence extends EndpointStep {
    //This doesn't increment the cell number because for all we know we still want to save other acquisition types to the same cell number.
    Path directory;
    AcquisitionManager acqMan;
    
    public AcquireFluorescence(Path directoryName, SequencerSettings settings) {
        //Acquires and saves a PWS cube returns the number of acquisitions saved: 1.
        super(settings);
        directory = directoryName;
        this.acqMan = Globals.acqManager();
    }
    
    public SequencerFunction getFunction() {
        return new SequencerFunction() {
            @Override
            public Integer applyThrows(Integer cellNum) {
                acqMan.setCellNum(cellNum);
                acqMan.setSavePath(directory.toString());
                acqMan.setFluorescenceSettings(settings);
                acqMan.acquireFluorescence();
                return 1; 
            } 
        };
    }
    
}
