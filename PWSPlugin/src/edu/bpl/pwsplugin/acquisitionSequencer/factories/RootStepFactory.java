/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class RootStepFactory extends StepFactory{
    //Should only exist once as the root of each experiment, sets the needed root parameters.
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return TimeSeriesUI.class;
    }
    
    @Override
    public Class<? extends SequencerSettings> getSettings() {
        return RootStepSettings.class;
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
        return "Root";
    }
    
    @Override
    public Consts.Category getCategory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.ROOT;
    }
}

class RootStep extends ContainerStep {
    public RootStep() {
        super(Consts.Type.ROOT);
    }
    
    @Override
    public SequencerFunction getFunction() { 
        RootStepSettings settings = (RootStepSettings) this.getSettings();
        SequencerFunction subStepFunc = getSubstepsFunction();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                Globals.acqManager().setSavePath(settings.directory);
                status = subStepFunc.apply(status);
                return status;
            }
        };    
    }
}

class RootStepSettings extends SequencerSettings {
    public String directory;
}

class RootStepUI extends BuilderJPanel<RootStepSettings> {
    JTextField directory = new JTextField(30);
    public RootStepUI() {
        super(new MigLayout("insets 0 0 0 0"), RootStepSettings.class);
        
        this.add(new JLabel("Root Directory:"), "gapleft push");
        this.add(directory);
    }
    
    @Override
    public void populateFields(RootStepSettings settings) {
        directory.setText(settings.directory);
    }
    
    @Override
    public RootStepSettings build() {
        RootStepSettings settings = new RootStepSettings();
        settings.directory = this.directory.getText();
        return settings;
    }
}