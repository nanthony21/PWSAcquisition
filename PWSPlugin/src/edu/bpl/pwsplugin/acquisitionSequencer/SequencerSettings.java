/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.List;
import org.micromanager.PositionList;

/**
 *
 * @author nick
 */
public class SequencerSettings {
    public static class SoftwareAutoFocusSettings extends JsonableParam {
        public String afPluginName = "OughtaFocus";
    }

    public static class RootStepSettings extends JsonableParam {
        public String directory;
    }

    public static class PauseStepSettings extends JsonableParam {
        public String message;
    }

    public static class FocusLockSettings extends JsonableParam {
        public double zOffset = 0;
        public double preDelay = 1;
    }

    public static class EveryNTimesSettings extends JsonableParam {
        public Integer n = 2;
        public Integer offset = 0;
    }

    public static class ChangeConfigGroupSettings extends JsonableParam {
        public String configGroupName;
        public String configValue;
    }

    public static class AcquireTimeSeriesSettings extends JsonableParam {
        public int numFrames = 1;
        public double frameIntervalMinutes = 1;

    }

    public static class AcquirePositionsSettings extends JsonableParam {
        public PositionList posList = new PositionList();

    }

}
