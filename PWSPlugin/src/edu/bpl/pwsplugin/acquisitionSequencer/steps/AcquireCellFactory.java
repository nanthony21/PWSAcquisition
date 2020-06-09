/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.settings.AcquireCellUI;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionManagers.AcquisitionManager;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireCellFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return AcquireCellUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return AcquireCellSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return AcquireCell.class;
    }
    
    @Override
    public String getDescription() {
        return "Acquire PWS, Dynamics, and Fluorescence into a single folder.";
    }
    
    @Override
    public String getName() {
        return "Acquisition";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.ACQ;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.ACQ;
    }
}

class AcquireCell extends EndpointStep {
    //Represents the acquisition of a single "CellXXX" folder, it can contain multiple PWS, Dynamics, and Fluorescence acquisitions.
    public AcquireCell() {
        super(new AcquireCellSettings(), Consts.Type.ACQ);
    }
    
    @Override
    public SequencerFunction getStepFunction() { //TODO save sequencer metadata (time step, position name, etc.)
        AcquireCellSettings settings = (AcquireCellSettings) this.getSettings();
        AcquisitionManager acqMan = Globals.acqManager();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception{
                status.setCellNum(status.getCellNum()+1);
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
                status.allowPauseHere();
                return status;
            }
        };
    }

    @Override
    protected Step.SimulatedStatus simulateRun(Step.SimulatedStatus status) {
        status.requiredPaths.add(Paths.get(status.workingDirectory, String.format("Cell%d", status.cellNum)).toString());
        status.cellNum++;
        return status;
    }
}


