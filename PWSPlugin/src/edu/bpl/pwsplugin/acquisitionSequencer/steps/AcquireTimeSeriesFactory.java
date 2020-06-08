/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;


/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireTimeSeriesFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return TimeSeriesUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.AcquireTimeSeriesSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return AcquireTimeSeries.class;
    }
    
    @Override
    public String getDescription() {
        return "Perform enclosed steps at multiple time points.";
    }
    
    @Override
    public String getName() {
        return "Time Series";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.SEQ;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.TIME;
    }
}


class TimeSeriesUI extends SingleBuilderJPanel<SequencerSettings.AcquireTimeSeriesSettings> {
    JSpinner numFrames;
    JSpinner frameIntervalMinutes;
    
    public TimeSeriesUI() {
        super(new MigLayout("insets 5 0 0 0"), SequencerSettings.AcquireTimeSeriesSettings.class);
        
        numFrames = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));
        frameIntervalMinutes = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000000000.0, 1.0));
        ((JSpinner.DefaultEditor) numFrames.getEditor()).getTextField().setColumns(6);
        ((JSpinner.DefaultEditor) frameIntervalMinutes.getEditor()).getTextField().setColumns(6);

                
        
        this.add(new JLabel("Number of time frames:"), "gapleft push");
        this.add(numFrames, "wrap");
        this.add(new JLabel("Frame Interval (minutes):"), "gapleft push");
        this.add(frameIntervalMinutes);
    }
    
    @Override
    public Map<String, Object> getPropertyFieldMap() {
        HashMap<String, Object> m = new HashMap<>();
        m.put("numFrames", numFrames);
        m.put("frameIntervalMinutes", frameIntervalMinutes);
        return m;
    }
}

class AcquireTimeSeries extends ContainerStep {
    public AcquireTimeSeries() {
        super(new AcquireCellSettings(), Consts.Type.TIME);
    }
    
    @Override 
    public SequencerFunction getStepFunction() {
        SequencerFunction stepFunction = super.getSubstepsFunction();
        SequencerSettings.AcquireTimeSeriesSettings settings = (SequencerSettings.AcquireTimeSeriesSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                //TIMESERIES execute acquisitionFunHandle repeatedly at a specified time
                //interval. the handle must take as input the Cell number to start at. It
                //will return the number of new acquisitions that it tood.
                double lastAcqTime = 0;
                for (int k=0; k<settings.numFrames; k++) {
                    // wait for the specified frame interval before proceeding to next frame
                    Integer msgId = status.newStatusMessage(""); //This will be updated below.
                    if (k!=0) { //No pause for the first iteration
                        int count = 0;
                        while ((System.currentTimeMillis() - lastAcqTime)/60000 < settings.frameIntervalMinutes) {
                            String msg = String.format("Waiting %.1f minutes before acquiring next frame", settings.frameIntervalMinutes - (System.currentTimeMillis() - lastAcqTime)/60000);
                            status.updateStatusMessage(msgId, msg);
                            count++;
                            Thread.sleep(500);
                        }   
                        if (count == 0) {
                            status.updateStatusMessage(msgId, String.format("Acquisition took %.1f seconds. Longer than the frame interval.", (System.currentTimeMillis() - lastAcqTime)/1000));
                        }
                    }
                    lastAcqTime = System.currentTimeMillis(); //Save the current time so we can figure out when to start the next acquisition.
                    status = stepFunction.apply(status);
                    status.newStatusMessage(String.format("Finished time step %d of %d", k+1, settings.numFrames));
                }
                return status;
            }
        };
    }
    
    @Override
    public Double numberNewAcqs() {
        Double acqsPerIteration = super.numberNewAcqsOneIteration();
        Double acqs = acqsPerIteration * ((SequencerSettings.AcquireTimeSeriesSettings) this.getSettings()).numFrames;
        return acqs;
    }
    
    @Override
    public List<String> requiredRelativePaths(Integer startingCellNum) {
        List<String> paths = new ArrayList<>();
        int numIterations = ((SequencerSettings.AcquireTimeSeriesSettings) this.getSettings()).numFrames;
        Integer cellNum = startingCellNum;
        for (int i=0; i<numIterations; i++) {
            paths.addAll(super.requiredRelativePaths(cellNum));
            cellNum += new Double(Math.floor(this.numberNewAcqsOneIteration())).intValue();
        }
        return paths;
    }
}
