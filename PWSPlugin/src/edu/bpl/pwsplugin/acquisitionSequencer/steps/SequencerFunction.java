/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.ThrowingFunction;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author nick
 */
@FunctionalInterface
public interface SequencerFunction extends ThrowingFunction<AcquisitionStatus, AcquisitionStatus> {
    
}
