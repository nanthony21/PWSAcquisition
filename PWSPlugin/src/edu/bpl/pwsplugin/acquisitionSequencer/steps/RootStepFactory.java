/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import com.google.gson.Gson;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.DirectorySelector;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.FileWriter;
import java.nio.file.Paths;
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
        super(new SequencerSettings.RootStepSettings(), Consts.Type.ROOT);
    }
    
    @Override
    public SequencerFunction getStepFunction() { 
        SequencerSettings.RootStepSettings settings = (SequencerSettings.RootStepSettings) this.getSettings();
        SequencerFunction subStepFunc = getSubstepsFunction();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                status.setCellNum(0);
                status.setSavePath(settings.directory);
                RootStep.this.saveToJson(Paths.get(settings.directory, "sequence.pwsseq").toString()); //Save the sequence to file for retrospect.
                status = subStepFunc.apply(status);
                return status;
            }
        };    
    }
    
    @Override
    public Double numberNewAcqs() { return this.numberNewAcqsOneIteration(); }
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