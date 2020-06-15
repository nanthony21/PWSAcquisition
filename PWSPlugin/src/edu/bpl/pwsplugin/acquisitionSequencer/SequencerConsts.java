/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.acquisitionSequencer.factory.AcquireCellFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.AcquireFromPositionListFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.AcquireTimeSeriesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.ChangeConfigGroupFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.EnterSubfolderFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.EveryNTimesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.FocusLockFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.PauseFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.SoftwareAutofocusFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.ZStackFactory;

/**
 *
 * @author nick
 */
public class SequencerConsts {
    public enum Type {
        ACQ,
        PFS,
        POS,
        TIME,
        AF,
        CONFIG,
        PAUSE,
        EVERYN,
        ROOT,
        SUBFOLDER,
        ZSTACK;
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
                return "Acquisitions";
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
                return new SoftwareAutofocusFactory();
            case PFS:
                return new FocusLockFactory();
            case POS:
                return new AcquireFromPositionListFactory();
            case TIME:
                return new AcquireTimeSeriesFactory();
            case CONFIG:
                return new ChangeConfigGroupFactory();
            case PAUSE:
                return new PauseFactory();
            case EVERYN:
                return new EveryNTimesFactory();
            case ROOT:
                return new RootStepFactory();
            case SUBFOLDER:
                return new EnterSubfolderFactory();
            case ZSTACK:
                return new ZStackFactory();
        } 
        throw new RuntimeException("Shouldn't get here.");
    }
    
    public static void registerGson() {
        for (Type t : Type.values()) {
            getFactory(t).registerGson();
        }
    }
}
