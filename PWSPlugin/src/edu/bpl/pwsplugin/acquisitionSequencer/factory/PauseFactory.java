/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.PauseStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class PauseFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return PauseStepUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.PauseStepSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return PauseStep.class;
    }
    
    @Override
    public String getDescription() {
        return "Open a dialog window and pause execution until the dialog is closed.";
    }
    
    @Override
    public String getName() {
        return "Pause";
    }
    
    @Override
    public SequencerConsts.Category getCategory() {
        return SequencerConsts.Category.UTIL;
    }

    @Override
    public SequencerConsts.Type getType() {
        return SequencerConsts.Type.PAUSE;
    }
}

class PauseStepUI extends BuilderJPanel<SequencerSettings.PauseStepSettings>{
    JTextArea message = new JTextArea();
    
    public PauseStepUI() {
        super(new MigLayout("insets 0 0 0 0, fill"), SequencerSettings.PauseStepSettings.class);
        
        //message.setPreferredSize(new Dimension(100, 100));
        message.setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.add(new JLabel("Message:"), "wrap");
        this.add(message, "w 100%, h 100%"); //This does the same as "grow" except that grow wasn't working for some reason here.
    }
    
    @Override
    public SequencerSettings.PauseStepSettings build() {
        SequencerSettings.PauseStepSettings settings = new SequencerSettings.PauseStepSettings();
        settings.message = message.getText();
        return settings;
    }
    
    @Override
    public void populateFields(SequencerSettings.PauseStepSettings settings) {
        this.message.setText(settings.message);
    }
}
