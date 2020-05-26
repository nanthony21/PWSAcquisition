/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.FocusLockUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.TimeSeriesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireTimeSeries;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.FocusLock;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class FocusLockFactory implements StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return FocusLockUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return FocusLockSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return FocusLock.class;
    }
    
    @Override
    public String getDescription() {
        return "Engage continuous hardware autofocus";
    }
    
    @Override
    public String getName() {
        return "Optical Focus Lock";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.UTIL;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.PFS;
    }
}