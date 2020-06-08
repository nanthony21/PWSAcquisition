/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;

/**
 *
 * @author nick
 */
public class ZStackFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return 
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.ZStackSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return
    }
    
    @Override
    public String getDescription() {
        return "Repeat the enclosed steps at evenly space locations along the Z axis.";
    }
    
    @Override 
    public String getName() {
        return "Z-Stack";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.SEQ;
    }
    
    @Override 
    public Consts.Type getType() {
        return Consts.Type.ZSTACK;
    }
}
    
