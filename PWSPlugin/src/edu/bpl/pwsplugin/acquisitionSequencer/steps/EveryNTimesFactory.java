/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class EveryNTimesFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return EveryNTimesUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.EveryNTimesSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return EveryNTimes.class;
    }
    
    @Override
    public String getDescription() {
        return "Execute sub-steps once every `N` iterations of this this step. Offset the cycle by `offset` iterations.";
    }
    
    @Override
    public String getName() {
        return "Once per `N` iterations";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.LOGIC;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.EVERYN;
    }
}

class EveryNTimes extends ContainerStep {
    int iteration = 0;
    
    public EveryNTimes() {
        super(Consts.Type.EVERYN);
    }
    
    @Override
    public SequencerFunction stepFunc() {
        SequencerFunction stepFunction = super.getSubstepsFunction();
        SequencerSettings.EveryNTimesSettings settings = (SequencerSettings.EveryNTimesSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                if (((iteration + settings.offset) % settings.n) == 0) {
                    status.newStatusMessage(String.format("EveryNTimes: Running substep on iteration %d", iteration));
                    status = stepFunction.apply(status);
                }
                iteration++;
                return status;
            } 
        };
    }
    
    @Override
    public Double numberNewAcqs() { //This is fractional since on some iterations nothing will happen
        Double oneIter = this.numberNewAcqsOneIteration();
        SequencerSettings.EveryNTimesSettings settings = (SequencerSettings.EveryNTimesSettings) this.getSettings();
        return oneIter / settings.n;
    }
}

class EveryNTimesUI extends BuilderJPanel<SequencerSettings.EveryNTimesSettings> {
    private JSpinner n;
    private JSpinner offset;
    
    public EveryNTimesUI() {
        super(new MigLayout("insets 0 0 0 0"), SequencerSettings.EveryNTimesSettings.class);
        
        n = new JSpinner(new SpinnerNumberModel(2, 1, 1000, 1));
        offset = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        
        this.add(new JLabel("N:"), "gapleft push");
        this.add(n, "wrap");
        this.add(new JLabel("Offset:"), "gapleft push");
        this.add(offset, "wrap");
    }
    
    public SequencerSettings.EveryNTimesSettings build() {
        SequencerSettings.EveryNTimesSettings settings = new SequencerSettings.EveryNTimesSettings();
        settings.n = (Integer) this.n.getValue();
        settings.offset = (Integer) this.offset.getValue();
        return settings;
    }
    
    public void populateFields(SequencerSettings.EveryNTimesSettings settings) {
        this.n.setValue(settings.n);
        this.offset.setValue(settings.offset);
    }
}
