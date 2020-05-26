/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.ChangeConfigGroupUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.EveryNTimesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.ChangeConfigGroupSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.EveryNTimesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ChangeConfigGroup;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EveryNTimes;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class EveryNTimesFactory implements StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return EveryNTimesUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return EveryNTimesSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return EveryNTimes.class;
    }
    
    @Override
    public String getDescription() {
        return "Execute sub-steps once every `N` iterations of this this step. Offset the cycle by `offset` iterations.";
    }
    
    @Override
    public String getName() {
        return "Run once per `N` iterations";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.LOGIC;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.EVERYN;
    }
}