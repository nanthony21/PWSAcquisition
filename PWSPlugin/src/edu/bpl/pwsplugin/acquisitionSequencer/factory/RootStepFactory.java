/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.acquisitionSequencer.steps.RootStep;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class RootStepFactory extends StepFactory{
    //Should only exist once as the root of each experiment, sets the needed root parameters.
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return RootStepUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.RootStepSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return RootStep.class;
    }
    
    @Override
    public String getDescription() {
        return "Initial settings for the experiment.";
    }
    
    @Override
    public String getName() {
        return "Initialization";
    }
    
    @Override
    public SequencerConsts.Category getCategory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SequencerConsts.Type getType() {
        return SequencerConsts.Type.ROOT;
    }
}

class RootStepUI extends BuilderJPanel<SequencerSettings.RootStepSettings> {
    DirectorySelector directory = new DirectorySelector(DirectorySelector.DefaultMMFunctions.MMDataSetDirectory);
    
    public RootStepUI() {
        super(new MigLayout("insets 0 0 0 0"), SequencerSettings.RootStepSettings.class);
        
        this.add(new JLabel("Root Directory:"), "gapleft push");
        this.add(directory);
    }
    
    @Override
    public void populateFields(SequencerSettings.RootStepSettings settings) {
        directory.setText(settings.directory);
    }
    
    @Override
    public SequencerSettings.RootStepSettings build() {
        SequencerSettings.RootStepSettings settings = new SequencerSettings.RootStepSettings();
        settings.directory = this.directory.getText();
        return settings;
    }
}