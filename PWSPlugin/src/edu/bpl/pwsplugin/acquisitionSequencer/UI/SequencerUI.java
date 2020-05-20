/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.StepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import net.miginfocom.swing.MigLayout;
        

/**
 *
 * @author nick
 */
public class SequencerUI extends JPanel {
    SequenceTree seqTree = new SequenceTree();
    NewStepsTree newStepsTree = new NewStepsTree();
    SettingsPanel settingsPanel = new SettingsPanel(seqTree, newStepsTree);
    JButton runButton = new JButton("Run");
    AcquisitionThread acqThread;
    
    public SequencerUI() {
        super(new MigLayout());

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.runButton.addActionListener((evt)->{  
            try {
                Step rootStep = SequencerUI.compileSequenceNodes((DefaultMutableTreeNode)seqTree.model().getRoot());
                SequencerFunction rootFunc = rootStep.getFunction();
                acqThread = new AcquisitionThread(rootFunc, 1);
                acqThread.execute();
            } catch (IllegalStateException | InstantiationException | IllegalAccessException e) {
                Globals.mm().logs().showError(e);
            }
            int a = 1; //Debug breakpoint here
        }); //Run starting at cell 1.
        
        JLabel l = new JLabel("Sequence");
        l.setFont(new Font("serif", Font.BOLD, 12));
        this.add(l);
        l = new JLabel("Available Step Types");
        l.setFont(new Font("serif", Font.BOLD, 12));
        this.add(l);
        this.add(settingsPanel, "wrap, spany 2");
        this.add(seqTree, "growy");
        this.add(newStepsTree, "growy, wrap");
        this.add(runButton, "spanx");
    }
    
    private static Step compileSequenceNodes(DefaultMutableTreeNode parent) throws InstantiationException, IllegalAccessException {
        //Only the Root can be DefaultMutableTreeNode, the rest better be StepNodes
        //Recursively compile a StepNode and it's children into a step which can be passed to the acquisition engine.   
        if (parent.getAllowsChildren()) {
            if (parent.getChildCount() == 0) {
                throw new IllegalStateException(String.format("%s container-node may not be empty", parent.toString()));
            }
            List<Step> l = new ArrayList<>();
            for (int i=0; i<parent.getChildCount(); i++) {  
                l.add(compileSequenceNodes((StepNode) parent.getChildAt(i)));
            } 
            ContainerStep step;
            if (!(parent instanceof StepNode)) { ///The only time we should get here is when we compile the root, which is not a step node. Treat it as just a generic container that runs substeps in sequence.
                step = new ContainerStep();        
            } else {
                step = (ContainerStep) Consts.getStepObject(((StepNode) parent).getType()).newInstance();
                step.setSettings(((StepNode) parent).getSettings());
            }
            step.setSubSteps(l);
            return step;
        } else {
            EndpointStep step = (EndpointStep) Consts.getStepObject(((StepNode) parent).getType()).newInstance();
            return step;
        }
    }
}

class SequencerRunningDlg extends JDialog {
    JLabel statusMsg = new JLabel();
    JLabel cellNum = new JLabel();
    PauseButton pauseButton = new PauseButton(true);
    JButton cancelButton = new JButton("Cancel");
    JProgressBar progress = new JProgressBar();
    
    public SequencerRunningDlg(Frame owner, String title) {
        super(owner, title, true);
        
        JPanel contentPane = new JPanel(new MigLayout());
        this.setContentPane(contentPane);
        contentPane.add(new JLabel("Status: "));
        contentPane.add(statusMsg, "wrap");
        contentPane.add(new JLabel("Acquiring: Cell"));
        contentPane.add(cellNum, "wrap");
        contentPane.add(progress, "wrap");
        contentPane.add(pauseButton);
        contentPane.add(cancelButton);
    }
}

class AcquisitionThread extends SwingWorker<AcquisitionStatus, AcquisitionStatus> {
    SequencerFunction rootFunc;
    private final AcquisitionStatus startingStatus;
    private AcquisitionStatus currentStatus;
    private final Function<AcquisitionStatus, Void> publishCallback;
    private SequencerRunningDlg dlg;
    
    
    public AcquisitionThread(SequencerFunction rootFunc, Integer startingCellNum, SequencerRunningDlg dlg) {
        this.rootFunc = rootFunc;  
        publishCallback = (status) -> { this.publish(status); return null; };
        startingStatus = new AcquisitionStatus(publishCallback);
        startingStatus.currentCellNum = startingCellNum;
        currentStatus = startingStatus;
        this.dlg = dlg;
    }
    
    @Override
    public AcquisitionStatus doInBackground() {
        try {
            AcquisitionStatus status = rootFunc.apply(this.startingStatus);
            this.currentStatus = status;
        } catch (Exception ie) {
            Globals.mm().logs().logError(ie);
        }
        SwingUtilities.invokeLater(() -> {
            finished();
        });
        return this.currentStatus;
    }
    
    public void finished() {
        dlg.
    }
    
    @Override
    protected void process(List<AcquisitionStatus> chunks) {
        currentStatus = chunks.get(chunks.size() - 1);
        //setProgress(
    }
    
    //public void done() This method has a bug where it can run before the thread actually exits. Use invokelater instead.
}