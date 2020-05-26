/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class StepFactory {
    public abstract Class<? extends BuilderJPanel> getUI();
    public abstract Class<? extends JsonableParam> getSettings();
    public abstract Class<? extends Step> getStep();
    public abstract String getDescription();
    public abstract String getName();
    public abstract Consts.Category getCategory();
    public abstract Consts.Type getType();
    
    public BuilderJPanel createUI() throws InstantiationException, IllegalAccessException {
        return getUI().newInstance();
    }
    
    public Step createStep() throws InstantiationException, IllegalAccessException {
        return getStep().newInstance();
    }
    
    public void registerGson() {
        JsonableParam.registerClass(getSettings());
        JsonableParam.registerClass(getStep());
    }
}