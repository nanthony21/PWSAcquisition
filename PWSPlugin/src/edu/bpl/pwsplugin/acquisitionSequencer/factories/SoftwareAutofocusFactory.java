/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.EveryNTimesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.SoftwareAutoFocusUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.EveryNTimesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EveryNTimes;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SoftwareAutofocus;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class SoftwareAutofocusFactory implements StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return SoftwareAutoFocusUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return SoftwareAutoFocusSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return SoftwareAutofocus.class;
    }
    
    @Override
    public String getDescription() {
        return "Run a software autofocus routine.";
    }
    
    @Override
    public String getName() {
        return "Software Autofocus";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.UTIL;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.AF;
    }
}