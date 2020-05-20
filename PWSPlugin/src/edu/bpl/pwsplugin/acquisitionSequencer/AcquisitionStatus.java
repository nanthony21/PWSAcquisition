/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquisitionStatus {
    public Integer currentCellNum;
    public String statusMsg;
    
    public AcquisitionStatus(AcquisitionStatus status) {
        currentCellNum = status.currentCellNum;
        statusMsg = status.statusMsg;
    }
}
