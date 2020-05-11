/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.stepSettings;

import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class TimeSeriesUI extends SingleBuilderJPanel<AcquireTimeSeriesSettings> {
    JSpinner numFrames;
    JSpinner frameIntervalMinutes;
    
    public TimeSeriesUI() {
        super(new MigLayout(), AcquireTimeSeriesSettings.class);
        
        numFrames = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));
        frameIntervalMinutes = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 10000000000.0, 1.0));
        
        this.add(new JLabel("Number of time frames:"));
        this.add(numFrames, "wrap");
        this.add(new JLabel("Frame Interval (minutes):"));
        this.add(frameIntervalMinutes);
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        HashMap<String, Object> m = new HashMap<>();
        m.put("numFrames", numFrames);
        m.put("frameIntervalMinues", frameIntervalMinutes);
        return m;
    }
}
