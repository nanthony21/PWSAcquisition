/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author nick
 */
public class AcquireTimeSeriesSettings extends SequencerSettings {
    public int numFrames = 1;
    public double frameIntervalMinutes = 1;
    
}
