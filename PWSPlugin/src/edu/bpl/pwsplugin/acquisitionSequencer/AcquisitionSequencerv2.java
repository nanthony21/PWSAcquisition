/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquisitionSequencerv2 {
    SwingWorker<AcquisitionStatus, AcquisitionStatus> acqThread;
    
    public void runSequence(Step rootStep, Integer startingCellNum) { //TODO run in another thread.
        SequencerFunction rootFunc = rootStep.getFunction();
        acqThread = new AcquisitionThread(rootFunc, startingCellNum);
        acqThread.execute();
    }
}


class AcquisitionThread extends SwingWorker<AcquisitionStatus, AcquisitionStatus> {
    SequencerFunction rootFunc;
    private AcquisitionStatus startingStatus = new AcquisitionStatus();
    private AcquisitionStatus endingStatus;
    
    public AcquisitionThread(SequencerFunction rootFunc, Integer startingCellNum) {
        this.rootFunc = rootFunc;
        this.startingStatus.currentCellNum = startingCellNum;
    }
    
    @Override
    public AcquisitionStatus doInBackground() {
        try {
            AcquisitionStatus status = rootFunc.apply(this.startingStatus);
            this.endingStatus = status;
        } catch (Exception ie) {
            this.endingStatus = new AcquisitionStatus();
            this.endingStatus.currentCellNum = 1;
            Globals.mm().logs().logError(ie);
        }
        SwingUtilities.invokeLater(() -> {
            //TODO open a dialog with the results.
        });
        return this.endingStatus;
    }
    
    //public void done() This method has a bug where it can run before the thread actually exits. Use invokelater instead.

}