/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import org.micromanager.internal.utils.FileDialogs;

/**
 *
 * @author nick
 */
public abstract class Step extends JsonableParam {
    private SequencerSettings settings; 

    public final SequencerSettings getSettings() { return settings; }
    
    public final void setSettings(SequencerSettings settings) { this.settings = settings; }
    
    public abstract SequencerFunction getFunction();
    
    public static final FileDialogs.FileType FILETYPE = new FileDialogs.FileType("PWS Acquisition Sequence", "Sequence (.pwsseq)", "newAcqSequence.pwsseq", true, "pwsseq");
    
    public static void registerWithGSON() {
        JsonableParam.registerClass(SoftwareAutofocus.class);
        JsonableParam.registerClass(FocusLock.class);
        JsonableParam.registerClass(ContainerStep.class);
        JsonableParam.registerClass(AutoShutter.class);
        JsonableParam.registerClass(AcquireTimeSeries.class);
        JsonableParam.registerClass(AcquireFromPositionList.class);
        JsonableParam.registerClass(AcquireCell.class);
    }
}
