/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.utils.JsonableParam;
import org.micromanager.PositionList;

/**
 *
 * @author nick
 */
public class SequencerSettings {
    public static class SoftwareAutoFocusSettings extends JsonableParam {
        //public String afPluginName = "OughtaFocus";
        public double exposureMs = 30;
    }

    public static class RootStepSettings extends JsonableParam {
        public String directory = "";
        public String description = "";
    }

    public static class PauseStepSettings extends JsonableParam {
        public String message = "Paused";
    }

    public static class FocusLockSettings extends JsonableParam {
        public double delay = 1; //Seconds delay after focus
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
    
    public static class EnterSubfolderSettings extends JsonableParam {
        public String relativePath = "";
    }
    
    public static class ZStackSettings extends JsonableParam {
        public double intervalUm = 1.0;
        public int numStacks = 2;
        public boolean absolute = false;
        public double startingPosition = 0; //Only used if absolute is true
    }
}
