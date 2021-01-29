///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SimpleAcquisition;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class SimpleAcquisitionFactory extends StepFactory {
    @Override
    public  Class<? extends BuilderJPanel> getUI() {
        return SimpleAcquisitionUI.class;
    }
    
    @Override
    public  Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.SimpleAcquisitionSettings.class;
    }
    
    @Override
    public  Class<? extends Step> getStep() {
        return SimpleAcquisition.class;
    }
    
    @Override
    public  String getDescription() {
       return "A basic image or video acquisition." ;
    }
    
    @Override
    public  String getName() {
        return "Standard Acquisition";
    }
    
    @Override
    public  String getCategory() {
        return "Acquisition";
    }
    
    @Override
    public  SequencerConsts.Type getType() {
        return SequencerConsts.Type.STD_ACQ;
    }
}


class SimpleAcquisitionUI extends BuilderJPanel<SequencerSettings.SimpleAcquisitionSettings>{
    private final ImprovedComponents.Spinner numFrames_ = new ImprovedComponents.Spinner(new SpinnerNumberModel(1, 1, 999999999, 1));
    private final ImprovedComponents.FormattedTextField exposure_ = new ImprovedComponents.FormattedTextField(NumberFormat.getIntegerInstance());
    private final JTextField format_ = new JTextField();
    
    public SimpleAcquisitionUI() {
        super(new MigLayout(), SequencerSettings.SimpleAcquisitionSettings.class);
        super.add(new JLabel("# Frames:"));
        super.add(numFrames_, "wrap");
        super.add(new JLabel("Exposure (ms):"));
        super.add(exposure_, "wrap");
        super.add(new JLabel("Naming Format:"));
        super.add(format_);
        
        format_.setToolTipText("{i} a number that iterates upon each acquisition. {p} each position. {t} time");
    }
    

    @Override
    public SequencerSettings.SimpleAcquisitionSettings build() throws BuilderPanelException {
        SequencerSettings.SimpleAcquisitionSettings settings = new SequencerSettings.SimpleAcquisitionSettings();
        settings.numFrames = (Integer) numFrames_.getValue();
        settings.exposureMs = (Double) exposure_.getValue();
        settings.namingFormat = format_.getText();
        return settings;
    }

    @Override
    public void populateFields(SequencerSettings.SimpleAcquisitionSettings settings) throws BuilderPanelException {
        numFrames_.setValue(settings.numFrames);
        exposure_.setValue(settings.exposureMs);
        format_.setText(settings.namingFormat);
    }
    
}