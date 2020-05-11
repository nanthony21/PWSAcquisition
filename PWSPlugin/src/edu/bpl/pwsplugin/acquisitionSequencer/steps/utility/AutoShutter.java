/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps.utility;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.illumination.Illuminator;

/**
 *
 * @author nick
 */
public class AutoShutter implements ContainerStep {
    double delay;
    Step step;
    Illuminator illuminator;
    public AutoShutter(double delaySeconds, Step step, Illuminator illum) {
        delay = delaySeconds;
        this.step = step;
        illuminator = illum;
    }
    
    @Override
    public Integer applyThrows(Integer cellNum) throws Exception {
        //AUTOSHUTTER A function that turns on the lamp, waits `delay` seconds, runs the acquisitionHandle, then turns off the lamp.
        this.illuminator.setShutter(true);
        Globals.statusAlert().setText("Delaying acquisition while lamp warms up.");
        Thread.sleep((long)(this.delay*1000));
        Integer numOfNewAcqs = this.step.apply(cellNum);
        this.illuminator.setShutter(false);
        return numOfNewAcqs;
    }    
}
