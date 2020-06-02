/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.Globals;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquisitionStatus { //TODO make this thread safe.
    //This object acts as a go-between between the UI and the acquisition thread.
    private String currentPath;
    protected Integer currentCellNum; //The folder number we are currently acquiring.
    public List<String> statusMsg = new ArrayList<>(); //A string describing what is currently happening.
    private final Function<AcquisitionStatus, Void> publishCallBack; //This callback should link to the `publish` method of the swingworker running the acquisition thread.
    private final Function<Void, Void> pauseCallBack; // This callback should link to the `pausepoint` method of a pause button.
    
    public AcquisitionStatus(Function<AcquisitionStatus, Void> publishCallBack, Function<Void, Void> pauseCallBack) {
        //Create a new status object 
        this.publishCallBack = publishCallBack;
        this.pauseCallBack = pauseCallBack;
    }
    
    public AcquisitionStatus(AcquisitionStatus status) { //This isn't used and maybe thats a good thing, maybe a single sequence should just have a single status object that is completely mutable.
        //Copy an existing status object to a new object, avoids issues with this being a mutable object.
        currentPath = status.currentPath;
        currentCellNum = status.currentCellNum;
        statusMsg = status.statusMsg;
        publishCallBack = status.publishCallBack;
        pauseCallBack = status.pauseCallBack;
    }
    
    private void publish() {
        //Send a copy of this object back to the swingworker so it can be accessed from the `process` method. //TODO is this necesary, can't we just maintain a reference to a single object?
        publishCallBack.apply(this);
    }
    
    public Integer newStatusMessage(String message) {
        this.statusMsg.add(message);
        this.publish();
        return this.statusMsg.size()-1; //This can be used as a pointer to update the message later.
    }
    
    public void updateStatusMessage(Integer idx, String msg) { //Idx is the number that was returned by `newStatusMessage`
        this.statusMsg.set(idx, msg);
        this.publish();
    }
    
    public void allowPauseHere() {
        //If the pause button was armed then block this thread until it is disarmed.
        pauseCallBack.apply(null);
    }
    
    public String getSavePath() {
       return currentPath; 
    }
    
    public void setSavePath(String path) {
        Globals.acqManager().setSavePath(path);
        currentPath = path;
    }
    
    public void setCellNum(Integer num) {
        currentCellNum = num;
        Globals.acqManager().setCellNum(num);
        this.publish();
    }
    
    public Integer getCellNum() {
        return currentCellNum;
    }
}
