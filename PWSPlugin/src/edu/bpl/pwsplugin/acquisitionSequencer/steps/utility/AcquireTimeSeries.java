/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps.utility;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author nick
 */
public class AcquireTimeSeries implements ContainerStep {
    double frame_interval;
    int num_frames;
    Step step;
    
    public AcquireTimeSeries(double frameInterval, int numFrames, Step step) {
        frame_interval = frameInterval;
        num_frames = numFrames;
        this.step = step;
    }
    
    @Override
    public Integer applyThrows(Integer startingCellNum) throws Exception {
        //TIMESERIES execute acquisitionFunHandle repeatedly at a specified time
        //interval. the handle must take as input the Cell number to start at. It
        //will return the number of new acquisitions that it tood.
        int numOfNewAcqs = 0;
        double lastAcqTime = 0;
        for (int k=0; k<num_frames; k++) {
            // wait for the specified frame interval before proceeding to next frame
            if (k!=0) { //No pause for the first iteration
                int count = 0;
                while ((System.currentTimeMillis() - lastAcqTime)/1000 < frame_interval) {
                    String msg = String.format("Waiting %.1f seconds before acquiring next frame", frame_interval - (System.currentTimeMillis() - lastAcqTime)/1000);
                    Globals.statusAlert().setText(msg);
                    count++;
                    Thread.sleep(500);
                }   
                if (count == 0) {
                    Globals.statusAlert().setText(String.format("Acquistion took %.1f seconds. Longer than the frame interval.", (System.currentTimeMillis() - lastAcqTime)/1000));
                }
            }
            int saveNum = startingCellNum + numOfNewAcqs;
            lastAcqTime = System.currentTimeMillis(); //Save the current time so we can figure out when to start the next acquisition.
            numOfNewAcqs += step.apply(saveNum);
            String msg = String.format("Finished frame %d of %d", k, num_frames);
            Globals.mm().alerts().postAlert("PWS", null, msg);
        }
        return numOfNewAcqs;
    }
    
    
}
