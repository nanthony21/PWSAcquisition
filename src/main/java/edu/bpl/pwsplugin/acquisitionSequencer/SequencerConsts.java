/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.acquisitionSequencer.factory.AcquireCellFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.AcquireFromPositionListFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.AcquireTimeSeriesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.AutoShutterStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.BrokenStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.ChangeConfigGroupFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.EnterSubfolderFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.EveryNTimesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.FocusLockFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.PauseFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.SoftwareAutofocusFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.ZStackFactory;

/**
 *
 * @author nick
 */
public class SequencerConsts {
    public enum Type {
        ACQ,
        POS,
        TIME,
        PFS,
        AF,
        CONFIG,
        PAUSE,
        EVERYN,
        ROOT,
        SUBFOLDER,
        ZSTACK,
        BROKEN,
        AUTOSHUTTER;
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
            case BROKEN:
                return new BrokenStepFactory();
            case AUTOSHUTTER:
                return new AutoShutterStepFactory();
        } 
        throw new RuntimeException("Shouldn't get here.");
    }
    
    public static void registerGson() {
        for (Type t : Type.values()) {
            getFactory(t).registerGson();
        }
    }
}
