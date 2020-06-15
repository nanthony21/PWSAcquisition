/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class StepFactory {
    //A factory to provide access to a `Step` class, a JsonableParam settings class, and a UI JPanel to adjust the settings. The `Step` must have a no-args constructor to work with the GSON type adapter.
    public abstract Class<? extends BuilderJPanel> getUI();
    public abstract Class<? extends JsonableParam> getSettings();
    public abstract Class<? extends Step> getStep();
    public abstract String getDescription();
    public abstract String getName();
    public abstract SequencerConsts.Category getCategory();
    public abstract SequencerConsts.Type getType();
    
    public BuilderJPanel createUI() {
        try {    
            return getUI().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Step createStep() {
        try {
            Step step = getStep().newInstance();
            return step;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void registerGson() {
        JsonableParam.registerClass(getSettings());
    }
}
