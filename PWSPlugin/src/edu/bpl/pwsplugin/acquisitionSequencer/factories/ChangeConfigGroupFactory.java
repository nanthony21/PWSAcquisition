/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.ChangeConfigGroupUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.PauseStepUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.ChangeConfigGroupSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.PauseStepSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ChangeConfigGroup;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.PauseStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ChangeConfigGroupFactory implements StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return ChangeConfigGroupUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return ChangeConfigGroupSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return ChangeConfigGroup.class;
    }
    
    @Override
    public String getDescription() {
        return "Change one of the Micro-Manager configuration groups. E.G. you could change the objective, etc.";
    }
    
    @Override
    public String getName() {
        return "Change Configuration Group";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.UTIL;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.CONFIG;
    }
}