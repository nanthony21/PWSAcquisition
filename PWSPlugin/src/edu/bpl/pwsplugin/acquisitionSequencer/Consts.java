/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquireCellUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquirePostionsUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.ChangeConfigGroupUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.EveryNTimesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.FocusLockUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.PauseStepUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.SoftwareAutoFocusUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.TimeSeriesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.AcquireCellFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.ChangeConfigGroupSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.EveryNTimesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.PauseStepSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireCell;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireFromPositionList;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireTimeSeries;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ChangeConfigGroup;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EveryNTimes;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.FocusLock;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.PauseStep;
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
        CONFIG,
        PAUSE,
        EVERYN;
    }
    
    public enum Category {
        ACQ,
        SEQ,
        UTIL,
        LOGIC;
    }
    
    public static String getCategoryName(Category cat) {
        switch (cat) {
            case ACQ:
                return "Acqusitions";
            case SEQ:
                return "Sequencing";
            case UTIL:
                return "Utility";
            case LOGIC:
                return "Logical";
        }
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static StepFactory getFactory(Type type) {
        if (null != type) switch (type) {
            case ACQ:
                return new AcquireCellFactory();
            case AF:
            case PFS:
            case POS:
            case TIME:
            case CONFIG:
            case PAUSE:
            case EVERYN:
                return new AcquireCellFactory();
        } 
        throw new RuntimeException("Shouldn't get here.");
    }
    
    public static StepFactory getFactory(Class<? extends Step> stepClass) {
        return getFactory(getTypeFromStepClass(stepClass));
    }
    
    private static Type getTypeFromStepClass(Class<? extends Step> clazz) {
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
        } else if (clazz == PauseStep.class) {
            return Type.PAUSE;
        } else if (clazz == EveryNTimes.class) {
            return Type.EVERYN;
        }
        throw new RuntimeException(String.format("Shouldn't get here. Class is %s", clazz.getName()));
    }
 
    public static boolean isContainer(Type type) {
        return ContainerStep.class.isAssignableFrom(getFactory(type).getStep());
    }
}
