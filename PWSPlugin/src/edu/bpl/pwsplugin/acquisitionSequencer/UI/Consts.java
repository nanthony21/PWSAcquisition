/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquireCellUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquirePostionsUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.ChangeConfigGroupUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.FocusLockUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.SoftwareAutoFocusUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.TimeSeriesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.ChangeConfigGroupSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireCell;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireFromPositionList;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireTimeSeries;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ChangeConfigGroup;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
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
        AF,
        CONFIG;

    }
    
    public enum Category {
        ACQ,
        SEQ,
        UTIL;
    }
    
    public static String getCategoryName(Category cat) {
        switch (cat) {
            case ACQ:
                return "Acqusitions";
            case SEQ:
                return "Sequencing";
            case UTIL:
                return "Utility";
        }
        throw new RuntimeException("Shouldn't get here");
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
            case CONFIG:
                return "Change Configuration Group";
        }
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static String getDescription(Type type) {
        switch (type) {
            case ACQ:
                return "Acquire PWS, Dynamics, and Fluorescence into a single folder.";
            case PFS:
                return "Engage continuous hardware autofocus";
            case POS:
                return "Perform enclosed steps at each position in the list.";
            case TIME:
                return "Perform enclosed steps at multiple time points.";
            case AF:
                return "Run a software autofocus routine";   
            case CONFIG:
                return "Change one of the Micro-Manager configuration groups. E.G. you could change the objective, etc.";
        }
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static Category getCategory(Type type) {
        if (type == Type.ACQ) {
            return Category.ACQ;
        } else if (type == Type.POS || type == Type.TIME) {
            return Category.SEQ;
        } else if (type == Type.PFS || type == Type.AF || type == Type.CONFIG) {
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
        } else if (type == Type.CONFIG) {
            return ChangeConfigGroup.class;
        }
        throw new RuntimeException(String.format("Shouldn't get here. Type is %s", type.toString()));
    }
    
    public static Type getTypeFromStepClass(Class<? extends Step> clazz) {
        if (clazz == AcquireCell.class) {
            return Type.ACQ;
        } else if (clazz == SoftwareAutofocus.class) {
            return Type.AF;
        } else if (clazz == FocusLock.class) {
            return Type.PFS;
        } else if (clazz == AcquireFromPositionList.class) {
            return Type.POS;
        } else if (clazz == AcquireTimeSeries.class) {
            return Type.TIME;
        } else if (clazz == ChangeConfigGroup.class) {
            return Type.CONFIG;
        }
        throw new RuntimeException(String.format("Shouldn't get here. Class is %s", clazz.getName()));
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
        } else if (type == Type.CONFIG) {
            return ChangeConfigGroupUI.class;
        }
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static Class<? extends SequencerSettings> getSettingsClass(Type type) {
        if (type == Type.ACQ) {
            return AcquireCellSettings.class;
        } else if (type == Type.AF) {
            return SoftwareAutoFocusSettings.class;
        } else if (type == Type.PFS) {
            return FocusLockSettings.class;
        } else if (type == Type.POS) {
            return AcquirePositionsSettings.class;
        } else if (type == Type.TIME) {
            return AcquireTimeSeriesSettings.class;
        } else if (type == Type.CONFIG) {
            return ChangeConfigGroupSettings.class;
        }
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static boolean isContainer(Type type) {
        return ContainerStep.class.isAssignableFrom(getStepObject(type));
    }
}
