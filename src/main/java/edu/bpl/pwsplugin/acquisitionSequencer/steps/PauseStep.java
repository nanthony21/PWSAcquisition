/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class PauseStep extends EndpointStep<SequencerSettings.PauseStepSettings> {
    
    public PauseStep() {
        super(new SequencerSettings.PauseStepSettings(), SequencerConsts.Type.PAUSE);
    }

    @Override
    public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
        SequencerSettings.PauseStepSettings settings = this.settings;
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                SwingUtilities.invokeAndWait(() -> {
                    status.newStatusMessage("Pausing.");
                    PauseDlg dlg = new PauseDlg(settings.message);
                    dlg.setVisible(true); //This should block until the dialog is closed.
                    status.newStatusMessage("Resuming.");
                });
                return status;
            }
        };
    }

    @Override
    protected SimFn getSimulatedFunction() {
        return (Step.SimulatedStatus status) -> {
            return status;
        };
    }
    
}

class PauseDlg extends JDialog {
    JLabel messageLabel = new JLabel();
    JLabel timerLabel = new JLabel();
    JButton proceedButton = new JButton("Proceed");
    long startTime = System.currentTimeMillis();
    Timer timer;
    
    public PauseDlg(String msg) {
        super(Globals.frame(), "Acquisition Paused");
        this.setModal(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(Globals.frame());
        this.setResizable(false);
        
        messageLabel.setText("<html>" + msg + "</html>");
        timerLabel.setBorder(BorderFactory.createEtchedBorder());
        
        JPanel p = new JPanel(new MigLayout("fill"));
        p.add(messageLabel, "wrap, align center");
        p.add(timerLabel, "wrap, align center");
        p.add(proceedButton, "align center");
        this.setContentPane(p);
        
        proceedButton.addActionListener((evt) -> {
            this.dispose();
        });
        
        updateTime();
        timer = new Timer(1000, (evt)-> {
           updateTime();
        });
        timer.start();
        this.pack();
    }
    
    private void updateTime() {
        int seconds = (int) ((System.currentTimeMillis() - startTime) / 1000.0);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;
        seconds = seconds % 60;
        String timeString = String.format("%d:%d:%d", hours, minutes, seconds);
        timerLabel.setText(String.format("<html>Paused for:<B>%s</B></html>", timeString));
    }
    
    @Override
    public void dispose(){
        timer.stop();
        super.dispose();
    }
}