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

import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.AcquireCellFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.AcquireFromPositionListFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.AcquireTimeSeriesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.AutoShutterStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.BrokenStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.ChangeConfigGroupFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.EnterSubfolderFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.EveryNTimesFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.FocusLockFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.PauseFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.SoftwareAutofocusFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories.ZStackFactory;

/**
 *
 * @author nick
 */
public class SequencerConsts {
            
    public enum Type {  // Built-intypes
        ROOT,
        BROKEN
    }

    public static StepFactory getFactory(String type) {
        if (null != type) switch (type) {
            case "ROOT":
                return new RootStepFactory();
            case "BROKEN":
                return new BrokenStepFactory();
        } 
        throw new RuntimeException("Shouldn't get here.");
    }
    
    public static void registerGson() {
        for (Type t : Type.values()) {
            getFactory(t.name()).registerGson();
        }
    }
}
