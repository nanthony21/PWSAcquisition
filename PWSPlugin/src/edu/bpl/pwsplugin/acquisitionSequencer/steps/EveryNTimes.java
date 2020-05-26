/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.EveryNTimesSettings;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class EveryNTimes extends ContainerStep {
    int iteration = 0;
    
    @Override
    public SequencerFunction getFunction() {
        SequencerFunction stepFunction = super.getSubstepsFunction();
        EveryNTimesSettings settings = (EveryNTimesSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                if (((iteration + settings.offset) % settings.n) == 0) {
                    status.update(String.format("EveryNTimes: Running substep on iteration %d", iteration), status.currentCellNum);
                    status = stepFunction.apply(status);
                }
                iteration++;
                return status;
            } 
        };
    }
}
