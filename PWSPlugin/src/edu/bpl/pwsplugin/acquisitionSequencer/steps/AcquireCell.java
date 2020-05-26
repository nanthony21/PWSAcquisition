/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;


/**
 *
 * @author nick
 */
public class AcquireCell extends EndpointStep {
    //Represents the acquisition of a single "CellXXX" folder, it can contain multiple PWS, Dynamics, and Fluorescence acquisitions.
    public AcquireCell() {
        super(Consts.Type.ACQ);
    }
    
    @Override
    public SequencerFunction getFunction() {
        AcquireCellSettings settings = (AcquireCellSettings) this.getSettings();
        AcquisitionManager acqMan = Globals.acqManager();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception{ //TODO need to make the fluorescence not overwrite eachother.
                acqMan.setCellNum(status.currentCellNum);
                acqMan.setSavePath(settings.directory);
                for (FluorSettings flSettings : settings.fluorSettings) {
                    status.allowPauseHere();
                    status.update(String.format("Acquiring %s fluoresence", flSettings.filterConfigName), status.currentCellNum);
                    acqMan.setFluorescenceSettings(flSettings);
                    acqMan.acquireFluorescence();
                }
                if (settings.pwsSettings != null) {
                    status.allowPauseHere();
                    status.update("Acquiring PWS", status.currentCellNum);
                    acqMan.setPWSSettings(settings.pwsSettings);
                    acqMan.acquirePWS();
                }
                if (settings.dynSettings != null) {
                    status.allowPauseHere();
                    status.update("Acquiring Dynamics", status.currentCellNum);
                    acqMan.setDynamicsSettings(settings.dynSettings);
                    acqMan.acquireDynamics();
                }
                status.allowPauseHere();
                status.currentCellNum += 1;
                return status;
            }
        };
    }
}


