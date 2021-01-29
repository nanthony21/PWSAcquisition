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
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class SimpleAcquisitionFactory extends StepFactory {
    public  Class<? extends BuilderJPanel> getUI() {
        SimpleAcquisitionUI.class;
    }
    
    public  Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.SimpleAcquisitionSettings.class;
    }
    
    public  Class<? extends Step> getStep() {
        return SimpleAcquisition.class;
    }
    
    public  String getDescription() {
       return "A basic image or video acquisition." ;
    }
    
    public  String getName() {
        return "Standard Acquisition";
    }
    
    public  String getCategory() {
        return "Acquisition";
    }
    
    public  SequencerConsts.Type getType() {
        return SequencerConsts.Type.STD_ACQ;
    }
}


class SimpleAcquisitionUI extends BuilderJPanel<SequencerSettings.SimpleAcquisitionSettings>{
    private final ImprovedComponents.Spinner numFrames_ = new ImprovedComponents.Spinner(new SpinnerNumberModel(1, 1, 999999999, 1));
    private final ImprovedComponents.FormattedTextField exposure_ = new ImprovedComponents.FormattedTextField(//TODO add format);
    
    public SimpleAcquisitionUI() {
        super(new MigLayout(), SequencerSettings.SimpleAcquisitionSettings.class);
        super.add(new JLabel("# Frames:"));
        super.add(numFrames_, "wrap");
        super.add(new JLabel("Exposure (ms):"));
        super.add(exposure_, "wrap");
    }
    

    @Override
    public SequencerSettings.SimpleAcquisitionSettings build() throws BuilderPanelException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void populateFields(SequencerSettings.SimpleAcquisitionSettings t) throws BuilderPanelException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}