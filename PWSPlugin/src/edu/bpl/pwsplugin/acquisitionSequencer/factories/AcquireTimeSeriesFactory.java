/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquireCellUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.TimeSeriesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireCell;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireTimeSeries;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireTimeSeriesFactory  implements StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return TimeSeriesUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return AcquireTimeSeriesSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return AcquireTimeSeries.class;
    }
    
    @Override
    public String getDescription() {
        return "Perform enclosed steps at multiple time points.";
    }
    
    @Override
    public String getName() {
        return "Time Series";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.SEQ;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.TIME;
    }
}
