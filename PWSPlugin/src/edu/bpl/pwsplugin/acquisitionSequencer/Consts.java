/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.acquisitionSequencer.factories.AcquireCellFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.AcquireFromPositionListFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.AcquireTimeSeriesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.ChangeConfigGroupFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.EveryNTimesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.FocusLockFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.PauseFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.SoftwareAutofocusFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;

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
        EVERYN,
        ROOT;
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
        } 
        throw new RuntimeException("Shouldn't get here.");
    }

    public static boolean isContainer(Type type) {
        return ContainerStep.class.isAssignableFrom(getFactory(type).getStep());
    }
    
    public static void registerGson() {
        for (Type t : Type.values()) {
            getFactory(t).registerGson();
        }
    }
}
