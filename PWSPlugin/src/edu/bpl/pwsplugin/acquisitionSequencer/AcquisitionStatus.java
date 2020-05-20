/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import java.util.function.Function;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquisitionStatus { //TODO make this thread safe.
    //This object acts as a go-between between the UI and the acquisition thread.
    public Integer currentCellNum; //The folder number we are currently acquiring.
    public String statusMsg; //A string describing what is currently happening.
    private final Function<AcquisitionStatus, Void> publishCallBack; //This callback should link to the `publish` method of the swingworker running the acquisition thread.
    private final Function<Void, Void> pauseCallBack; // This callback should link to the `pausepoint` method of a pause button.
    
    public AcquisitionStatus(Function<AcquisitionStatus, Void> publishCallBack, Function<Void, Void> pauseCallBack) {
        //Create a new status object 
        this.publishCallBack = publishCallBack;
        this.pauseCallBack = pauseCallBack;
    }
    
    public AcquisitionStatus(AcquisitionStatus status) {
        //Copy an existing status object to a new object, avoids issues with this being a mutable object.
        currentCellNum = status.currentCellNum;
        statusMsg = status.statusMsg;
        publishCallBack = status.publishCallBack;
        pauseCallBack = status.pauseCallBack;
    }
    
    private void publish() {
        //Send a copy of this object back to the swingworker so it can be accessed from the `process` method.
        publishCallBack.apply(this);
    }
    
    public void update(String message, Integer cellNumber) {
        this.currentCellNum = cellNumber;
        this.statusMsg = message;
        this.publish();
    }
    
    public void allowPauseHere() {
        //If the pause button was armed then block this thread until it is disarmed.
        pauseCallBack.apply(null);
    }
}
