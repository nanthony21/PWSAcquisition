///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
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
import edu.bpl.pwsplugin.acquisitionSequencer.factory.SimpleAcquisitionFactory;
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
        STD_ACQ,
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
            case STD_ACQ:
                return new SimpleAcquisitionFactory();
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
