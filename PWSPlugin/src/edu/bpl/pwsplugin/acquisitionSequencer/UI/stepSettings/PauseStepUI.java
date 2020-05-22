/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.PauseStepSettings;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class PauseStepUI extends BuilderJPanel<PauseStepSettings>{
    JTextArea message = new JTextArea();
    
    public PauseStepUI() {
        super(new MigLayout("insets 0 0 0 0, fill"), PauseStepSettings.class);
        
        //message.setPreferredSize(new Dimension(100, 100));
        message.setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.add(new JLabel("Message:"), "wrap, shrink");
        this.add(message, "grow");
    }
    
    @Override
    public PauseStepSettings build() {
        PauseStepSettings settings = new PauseStepSettings();
        settings.message = message.getText();
        return settings;
    }
    
    @Override
    public void populateFields(PauseStepSettings settings) {
        this.message.setText(settings.message);
    }
}
