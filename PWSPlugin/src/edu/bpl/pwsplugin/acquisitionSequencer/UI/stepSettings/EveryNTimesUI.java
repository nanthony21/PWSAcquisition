/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.EveryNTimesSettings;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class EveryNTimesUI extends BuilderJPanel<EveryNTimesSettings> {
    private JSpinner n;
    private JSpinner offset;
    
    public EveryNTimesUI() {
        super(new MigLayout("insets 0 0 0 0"), EveryNTimesSettings.class);
        
        n = new JSpinner(new SpinnerNumberModel(2, 1, 1000, 1));
        offset = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        
        this.add(new JLabel("N:"));
        this.add(n, "wrap");
        this.add(new JLabel("Offset:"));
        this.add(offset, "wrap");
    }
    
    public EveryNTimesSettings build() {
        EveryNTimesSettings settings = new EveryNTimesSettings();
        settings.n = (Integer) this.n.getValue();
        settings.offset = (Integer) this.offset.getValue();
        return settings;
    }
    
    public void populateFields(EveryNTimesSettings settings) {
        this.n.setValue(settings.n);
        this.offset.setValue(settings.offset);
    }
}
