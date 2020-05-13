/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.settings.DynSettings;
import java.nio.file.Path;

/**
 *
 * @author nick
 */
public class AcquireDynamics extends EndpointStep {
    //This doesn't increment the cell number because for all we know we still want to save other acquisition types to the same cell number.
    Path directory;
    DynSettings settings;
    AcquisitionManager acqMan;
    
    public AcquireDynamics(Path directoryName, SequencerSettings settings) {
        //Acquires and saves a PWS cube returns the number of acquisitions saved: 1.
        super(settings);
        directory = directoryName;
        this.acqMan = Globals.acqManager();
    }
    
    @Override
    public SequencerFunction getFunction() {
        return new SequencerFunction() {
            @Override
            public Integer applyThrows(Integer cellNum) {
                acqMan.setCellNum(cellNum);
                acqMan.setSavePath(directory.toString());
                acqMan.setDynamicsSettings(settings);
                acqMan.acquireDynamics();
                return 1; 
            }   
        };
    }

}
