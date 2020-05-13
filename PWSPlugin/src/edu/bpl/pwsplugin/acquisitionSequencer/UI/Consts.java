/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquireCellUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquirePostionsUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.FocusLockUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.SoftwareAutoFocusUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.TimeSeriesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireCell;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireFromPositionList;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireTimeSeries;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.FocusLock;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SoftwareAutofocus;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author nick
 */
public class Consts {
    public enum Type {
        ACQ,
        PFS,
        POS,
        TIME,
        AF;
    }
    
    public enum Category {
        ACQ,
        SEQ,
        UTIL;
    }
    
    public static String getName(Type type) {
        switch (type) {
            case ACQ:
                return "Acquisition";
            case PFS:
                return "Optical Focus Lock";
            case POS:
                return "Multiple Positions";
            case TIME:
                return "Time Series";
            case AF:
                return "Software Autofocus";                  
        }
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static Category getCategory(Type type) {
        if (type == Type.ACQ) {
            return Category.ACQ;
        } else if (type == Type.POS || type == Type.TIME) {
            return Category.SEQ;
        } else if (type == Type.PFS || type == Type.AF) {
            return Category.UTIL;
        }
        throw new RuntimeException("Shouldn't get here");
    }   
    
    public static Class<? extends Step> getStepObject(Type type) {
        if (type == Type.ACQ) {
            return AcquireCell.class;
        } else if (type == Type.AF) {
            return SoftwareAutofocus.class;
        } else if (type == Type.PFS) {
            return FocusLock.class;
        } else if (type == Type.POS) {
            return AcquireFromPositionList.class;
        } else if (type == Type.TIME) {
            return AcquireTimeSeries.class;
        } 
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static Class<? extends BuilderJPanel> getUI(Type type) {
        if (type == Type.ACQ) {
            return AcquireCellUI.class;
        } else if (type == Type.AF) {
            return SoftwareAutoFocusUI.class;
        } else if (type == Type.PFS) {
            return FocusLockUI.class;
        } else if (type == Type.POS) {
            return AcquirePostionsUI.class;
        } else if (type == Type.TIME) {
            return TimeSeriesUI.class;
        } 
        throw new RuntimeException("Shouldn't get here");
    }
}
