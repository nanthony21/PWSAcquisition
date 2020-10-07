/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.BrokenStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class BrokenStepFactory extends StepFactory {
    //A step only used for a placeholder when we unsucessfully try to load a step.    
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return BrokenStepUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return JsonableParam.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return BrokenStep.class;
    }
    
    @Override
    public String getDescription() { return "This step failed to load and needs to be replaced."; }
    
    @Override
    public String getName() { return "BROKEN"; }
    
    @Override
    public String getCategory() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public SequencerConsts.Type getType() {
        return SequencerConsts.Type.BROKEN;
    }
}

class BrokenStepUI extends BuilderJPanel<JsonableParam> {
    public BrokenStepUI() {
        super(new MigLayout("insets 0 0 0 0"), JsonableParam.class);
        
        this.add(new JLabel("LOADING FAILED!!!"));
    }
    
    @Override
    public JsonableParam build() {
        return new JsonableParam();
    }
    
    @Override
    public void populateFields(JsonableParam settings) {}
}

