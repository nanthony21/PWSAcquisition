/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.ThrowingFunction;

/**
 *
 * @author nick
 */
@FunctionalInterface
public interface SequencerFunction extends ThrowingFunction<AcquisitionStatus, AcquisitionStatus> {
    
}
