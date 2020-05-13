/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author nick
 */
public class SequencerSettings extends JsonableParam {
    public static void registerWithGSON() {
        JsonableParam.registerClass(AcquirePositionsSettings.class);
        JsonableParam.registerClass(AcquireTimeSeriesSettings.class);
        JsonableParam.registerClass(SoftwareAutoFocusSettings.class);
        JsonableParam.registerClass(FocusLockSettings.class);
        JsonableParam.registerClass(AutoshutterSettings.class);
        JsonableParam.registerClass(AcquireCellSettings.class);
    }
}
