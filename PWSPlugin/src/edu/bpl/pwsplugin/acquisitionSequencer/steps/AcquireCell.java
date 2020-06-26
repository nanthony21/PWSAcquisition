/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author nick
 */
public class AcquireCell extends EndpointStep<AcquireCellSettings> {
    
    //Represents the acquisition of a single "CellXXX" folder, it can contain multiple PWS, Dynamics, and Fluorescence acquisitions.
    public AcquireCell() {
        super(new AcquireCellSettings(), SequencerConsts.Type.ACQ);
    }

    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
        AcquireCellSettings settings = this.getSettings();
        AcquisitionManager acqMan = Globals.acqManager();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                status.setCellNum(status.getCellNum() + 1);
                status.newStatusMessage(String.format("Acquiring Cell %d", status.getCellNum()));
                if (!settings.fluorSettings.isEmpty()) {
                    status.allowPauseHere();
                    acqMan.setFluorescenceSettings(settings.fluorSettings);
                    acqMan.acquireFluorescence();
                }
                if (settings.pwsSettings != null) {
                    status.allowPauseHere();
                    acqMan.setPWSSettings(settings.pwsSettings);
                    acqMan.acquirePWS();
                }
                if (settings.dynSettings != null) {
                    status.allowPauseHere();
                    acqMan.setDynamicsSettings(settings.dynSettings);
                    acqMan.acquireDynamics();
                }
                saveSequenceCoordsFile(status);
                status.allowPauseHere();
                return status;
            }
        };
    }
    
    private void saveSequenceCoordsFile(AcquisitionStatus status) throws IOException {
        JsonObject obj = status.coords().toJson();
        Path directory = FileSpecs.getCellFolderName(Paths.get(status.getSavePath()), status.getCellNum());
        String savePath = directory.resolve("sequencerCoords.json").toString();
        if (!directory.toFile().exists()) { directory.toFile().mkdirs(); } //Usually the cell folder should be created by the Image saving thread. In some cases it can get backed up, this will prevent a crash in that case.
        try (FileWriter w = new FileWriter(savePath)) {
            GsonUtils.getGson().toJson(obj, w);
        }
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
