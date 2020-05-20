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
public class AcquisitionStatus {
    public Integer currentCellNum;
    public String statusMsg;
    private final Function<AcquisitionStatus, Void> callBack;
    
    public AcquisitionStatus(Function<AcquisitionStatus, Void> callBack) {
        this.callBack = callBack;
    }
    
    public AcquisitionStatus(AcquisitionStatus status) {
        currentCellNum = status.currentCellNum;
        statusMsg = status.statusMsg;
        callBack = status.callBack;
    }
    
    public void publish() {
        callBack.apply(this);
    }
}
