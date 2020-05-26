/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public interface StepFactory {
    public Class<? extends BuilderJPanel> getUI();
    public Class<? extends SequencerSettings> getSettings();
    public Class<? extends Step> getStep();
    public String getDescription();
    public String getName();
    public Consts.Category getCategory();
    public Consts.Type getType();
}
