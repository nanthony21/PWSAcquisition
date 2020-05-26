/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquirePostionsUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireFromPositionList;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireFromPositionListFactory implements StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return AcquirePostionsUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return AcquirePositionsSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return AcquireFromPositionList.class;
    }
    
    @Override
    public String getDescription() {
        return "Perform enclosed steps at each position in the list.";
    }
    
    @Override
    public String getName() {
        return "Multiple Positions";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.SEQ;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.POS;
    }
}