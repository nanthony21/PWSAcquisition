/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.PauseStepSettings;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class PauseStep extends EndpointStep {
    @Override
    public SequencerFunction getFunction() {
        PauseStepSettings settings = (PauseStepSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                SwingUtilities.invokeAndWait(() -> {
                    PauseDlg dlg = new PauseDlg(settings.message);
                    dlg.setVisible(true); //This should block until the dialog is closed.
                });
                return status;
            }
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
        
        messageLabel.setText("<html>" + msg + "</html>");
        
        this.add(messageLabel, "wrap");
        this.add(timerLabel, "wrap");
        this.add(proceedButton);
        
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
        timerLabel.setText(String.format("<html><font size=18>Paused for: <B>%s</B></font></html>", timeString));
    }
    
    @Override
    public void dispose(){
        timer.stop();
        super.dispose();
    }
}