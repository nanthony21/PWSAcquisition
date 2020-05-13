/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author nick
 */
public class AcquireCell extends EndpointStep {
    //Represents the acquisition of a single "CellXXX" folder, it can contain multiple PWS, Dynamics, and Fluorescence acquisitions.
    Path directory;
    AcquirePWS  pws;
    AcquireDynamics dyn;
    List<AcquireFluorescence> fluor;
    
    public AcquireCell(Path directory, PWSSettings  pws,  DynSettings dyn, List<FluorSettings> fluor) {
        if (pws != null) {
            this.pws = new AcquirePWS(directory, pws);
        }
        if (dyn != null) {
            this.dyn = new AcquireDynamics(directory, dyn);
        }
        if (fluor != null) {
            Function<FluorSettings, AcquireFluorescence> constructStep = (setting)->{return new AcquireFluorescence(directory, setting);};
            this.fluor = fluor.stream().map(constructStep).collect(Collectors.toList()); //Build a list of fluorescence steps.
        } else {
            this.fluor = new ArrayList<>();
        }
        this.directory = directory;
    }
    
    @Override
    public SequencerFunction getFunction() {
        List<SequencerFunction> flFuncs = this.fluor.stream().map(AcquireFluorescence::getFunction).collect(Collectors.toList());
        SequencerFunction dynFunc = null;
        if (this.dyn != null) { this.dyn.getFunction(); }
        SequencerFunction pwsFunc = null;
        if (this.pws != null) { this.pws.getFunction(); }
        return new SequencerFunction() {
            @Override
            public Integer applyThrows(Integer saveNum) throws Exception{ //TODO need to make the fluorescence not overwrite eachother.
                for (SequencerFunction flFunc : flFuncs) {
                    flFunc.apply(saveNum);
                }
                if (pwsFunc != null) {
                    pwsFunc.apply(saveNum);
                }
                if (dynFunc != null) {
                    dynFunc.apply(saveNum);
                }

                return 1;
            }
        };
    }
}
