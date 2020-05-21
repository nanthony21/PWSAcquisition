/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import com.google.gson.Gson;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.ThrowingFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.ContainerStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.EndpointStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.StepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.micromanager.internal.utils.FileDialogs;
        

/**
 *
 * @author nick
 */
public class SequencerUI extends JPanel {
    SequenceTree seqTree = new SequenceTree();
    NewStepsTree newStepsTree = new NewStepsTree();
    SettingsPanel settingsPanel = new SettingsPanel(seqTree, newStepsTree);
    JButton runButton = new JButton("Run");
    JButton saveButton = new JButton("Save");
    JButton loadButton = new JButton("Load");
    AcquisitionThread acqThread;
    
    public SequencerUI() {
        super(new MigLayout());

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.runButton.addActionListener((evt) -> {  
            try {
                Step rootStep = SequencerUI.compileSequenceNodes((DefaultMutableTreeNode)seqTree.model().getRoot());
                SequencerFunction rootFunc = rootStep.getFunction();
                SequencerRunningDlg dlg = new SequencerRunningDlg(SwingUtilities.getWindowAncestor(this), "Acquisition Sequence Running");
                acqThread = new AcquisitionThread(rootFunc, 1, dlg);
                acqThread.execute();
            } catch (IllegalStateException | InstantiationException | IllegalAccessException e) {
                Globals.mm().logs().showError(e);
            }
            int a = 1; //Debug breakpoint here
        }); //Run starting at cell 1.
        
        this.saveButton.addActionListener((evt) -> {
            try { 
                Step rootStep = compileSequenceNodes((DefaultMutableTreeNode) seqTree.model().getRoot());
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                String path = FileDialogs.save(topFrame, "Save Sequence", Step.FILETYPE).getPath();
                rootStep.toJsonFile(path);
            } catch (InstantiationException | IllegalAccessException | IOException e) {
                Globals.mm().logs().logError(e);
            }
        });
        
        this.loadButton.addActionListener((evt) -> {
            try {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                String path = FileDialogs.openFile(topFrame, "Load Sequence", Step.FILETYPE).getPath();
                ContainerStep rootStep = (ContainerStep) JsonableParam.fromJsonFile(path, Step.class);
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
                for (Step subStep : rootStep.getSubSteps()) {
                    rootNode.add(loadNodeFromStep(subStep));
                }
                seqTree.model().setRoot(rootNode);
            } catch (FileNotFoundException e) {
                Globals.mm().logs().logError(e);
            }
        });
        
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
        this.add(saveButton);
        this.add(loadButton);
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
    
    private StepNode loadNodeFromStep(Step rootStep) {
        if (rootStep instanceof ContainerStep) {
            ContainerStepNode node = new ContainerStepNode(rootStep.getSettings(), Consts.getTypeFromStepClass(rootStep.getClass()));            
            for (Step subStep : ((ContainerStep) rootStep).getSubSteps()) {
                StepNode subNode = (StepNode) loadNodeFromStep(subStep);
                node.add(subNode);
            }
            return node;
        } else if (rootStep instanceof EndpointStep) {
            return new EndpointStepNode(rootStep.getSettings(), Consts.getTypeFromStepClass(rootStep.getClass()));
        }
        throw new RuntimeException("Should not get here.");
    }
}

class SequencerRunningDlg extends JDialog {
    JLabel statusMsg = new JLabel();
    JLabel cellNum = new JLabel();
    PauseButton pauseButton = new PauseButton(true);
    JButton cancelButton = new JButton("Cancel");
    JProgressBar progress = new JProgressBar();
    
    public SequencerRunningDlg(Window owner, String title) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        
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
    private SequencerRunningDlg dlg;
    
    
    public AcquisitionThread(SequencerFunction rootFunc, Integer startingCellNum, SequencerRunningDlg dlg) {
        this.rootFunc = rootFunc;  
        ThrowingFunction<AcquisitionStatus, Void> publishCallback = (status) -> { this.publish(status); return null; };
        ThrowingFunction<Void, Void> pauseCallback = (nullInput) -> { dlg.pauseButton.pausePoint(); return nullInput; };
        startingStatus = new AcquisitionStatus(publishCallback, pauseCallback);
        startingStatus.currentCellNum = startingCellNum;
        currentStatus = startingStatus;
        this.dlg = dlg;
        dlg.setVisible(true);
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
        dlg.dispose();
    }
    
    @Override
    protected void process(List<AcquisitionStatus> chunks) {
        currentStatus = chunks.get(chunks.size() - 1);
        //setProgress(
    }
    
    //public void done() This method has a bug where it can run before the thread actually exits. Use invokelater instead.
}