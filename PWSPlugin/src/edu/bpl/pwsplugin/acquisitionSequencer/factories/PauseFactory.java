/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class PauseFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return PauseStepUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.PauseStepSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return PauseStep.class;
    }
    
    @Override
    public String getDescription() {
        return "Open a dialog window and pause execution until the dialog is closed.";
    }
    
    @Override
    public String getName() {
        return "Pause";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.UTIL;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.PAUSE;
    }
}

class PauseStepUI extends BuilderJPanel<SequencerSettings.PauseStepSettings>{
    JTextArea message = new JTextArea();
    
    public PauseStepUI() {
        super(new MigLayout("insets 0 0 0 0, fill"), SequencerSettings.PauseStepSettings.class);
        
        //message.setPreferredSize(new Dimension(100, 100));
        message.setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.add(new JLabel("Message:"), "wrap");
        this.add(message, "w 100%, h 100%"); //This does the same as "grow" except that grow wasn't working for some reason here.
    }
    
    @Override
    public SequencerSettings.PauseStepSettings build() {
        SequencerSettings.PauseStepSettings settings = new SequencerSettings.PauseStepSettings();
        settings.message = message.getText();
        return settings;
    }
    
    @Override
    public void populateFields(SequencerSettings.PauseStepSettings settings) {
        this.message.setText(settings.message);
    }
}

class PauseStep extends EndpointStep {
    
    public PauseStep() {
        super(Consts.Type.PAUSE);
    }
    
    @Override
    public SequencerFunction getFunction() {
        SequencerSettings.PauseStepSettings settings = (SequencerSettings.PauseStepSettings) this.getSettings();
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
        timerLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        
        JPanel p = new JPanel(new MigLayout("fill, insets 0 0 0 0"));
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
        timerLabel.setText(String.format("<html>Paused:<br><B>%s</B></html>", timeString));
    }
    
    @Override
    public void dispose(){
        timer.stop();
        super.dispose();
    }
}