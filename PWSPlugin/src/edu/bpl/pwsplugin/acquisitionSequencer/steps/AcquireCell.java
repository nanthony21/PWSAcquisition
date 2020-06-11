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
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.nio.file.Paths;

/**
 *
 * @author nick
 */
public class AcquireCell extends EndpointStep<AcquireCellSettings> {
    
    //Represents the acquisition of a single "CellXXX" folder, it can contain multiple PWS, Dynamics, and Fluorescence acquisitions.
    public AcquireCell() {
        super(new AcquireCellSettings(), Consts.Type.ACQ);
    }

    @Override
    public SequencerFunction getStepFunction() {
        AcquireCellSettings settings = this.getSettings();
        AcquisitionManager acqMan = Globals.acqManager();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                status.setCellNum(status.getCellNum() + 1);
                status.newStatusMessage(String.format("Acquiring Cell %d", status.getCellNum()));
                for (FluorSettings flSettings : settings.fluorSettings) {
                    status.allowPauseHere();
                    //status.newStatusMessage(String.format("Acquiring %s fluoresence", flSettings.filterConfigName));
                    acqMan.setFluorescenceSettings(flSettings);
                    acqMan.acquireFluorescence();
                }
                if (settings.pwsSettings != null) {
                    status.allowPauseHere();
                    //status.newStatusMessage("Acquiring PWS");
                    acqMan.setPWSSettings(settings.pwsSettings);
                    acqMan.acquirePWS();
                }
                if (settings.dynSettings != null) {
                    status.allowPauseHere();
                    //status.newStatusMessage("Acquiring Dynamics");
                    acqMan.setDynamicsSettings(settings.dynSettings);
                    acqMan.acquireDynamics();
                }
                //TODO save the treepath too so the coords are unique. Add uuid for each Step to save with treepath. Don't save as stupid MM propertymap
                status.coords.toPropertyMap().saveJSON(FileSpecs.getCellFolderName(Paths.get(status.getSavePath()), status.getCellNum()).resolve("sequencerCoords.json").toFile(), false, false);
                status.allowPauseHere();
                return status;
            }
        };
    }

    @Override
    protected SimFn getSimulatedFunction() {
        return (Step.SimulatedStatus status) -> {
            status.cellNum++;
            status.requiredPaths.add(Paths.get(status.workingDirectory, String.format("Cell%d", status.cellNum)).toString());
            return status;
        };
    }
    
}
