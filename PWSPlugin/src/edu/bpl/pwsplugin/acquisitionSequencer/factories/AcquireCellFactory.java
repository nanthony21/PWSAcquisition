/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.SequencerUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquireCellUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireCell;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireCellFactory implements StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return AcquireCellUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return AcquireCellSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return AcquireCell.class;
    }
    
    @Override
    public String getDescription() {
        return "Acquire PWS, Dynamics, and Fluorescence into a single folder.";
    }
    
    @Override
    public String getName() {
        return "Acquisition";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.ACQ;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.ACQ;
    }
}
