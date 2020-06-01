/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.ThrowingFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.ContainerStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.EndpointStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.StepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.factories.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.micromanager.internal.utils.FileDialogs;
        

/**
 *
 * @author nick
 * //TODO this should be split into a file for UI-only functionality and the core functionality of the sequencer.
 */
public class SequencerUI extends BuilderJPanel<ContainerStep> {
    SequenceTree seqTree = new SequenceTree();
    NewStepsTree newStepsTree = new NewStepsTree();
    SettingsPanel settingsPanel = new SettingsPanel(seqTree, newStepsTree);
    JButton runButton = new JButton("Run");
    JButton saveButton = new JButton("Save");
    JButton loadButton = new JButton("Load");
    
    public SequencerUI() {
        super(new MigLayout("fill"), ContainerStep.class);

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.runButton.addActionListener((evt) -> {  
            try {
                Step rootStep = this.build();
                List<String> errors = verifySequence(rootStep);
                boolean success = resolveFileConflicts(rootStep);
                if (!success) { return; }
                if (!errors.isEmpty()) {
                    Globals.mm().logs().showError(String.join("\n", errors));
                    return;
                }
                rootStep.addCallback((status) ->{
                    status.newStatusMessage("Root callback");
                    return status;
                });
                SequencerFunction rootFunc = rootStep.getFunction();
                SequencerRunningDlg dlg = new SequencerRunningDlg(SwingUtilities.getWindowAncestor(this), "Acquisition Sequence Running", rootFunc);
            } catch (IllegalStateException  | BuilderPanelException e) {
                Globals.mm().logs().showError(e);
            }
        }); //Run starting at cell 1.
        
        this.saveButton.addActionListener((evt) -> {
            try { 
                Step rootStep = this.build();
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                String path = FileDialogs.save(topFrame, "Save Sequence", Step.FILETYPE).getPath();
                if(!path.endsWith(".pwsseq")) {
                    path = path + ".pwsseq"; //Make sure the extension is there.
                }
                rootStep.toJsonFile(path);
            } catch (IOException | BuilderPanelException e) {
                Globals.mm().logs().logError(e);
            }
        });
        
        this.loadButton.addActionListener((evt) -> {
            try {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                String path = FileDialogs.openFile(topFrame, "Load Sequence", Step.FILETYPE).getPath();
                ContainerStep rootStep = (ContainerStep) JsonableParam.fromJsonFile(path, Step.class);
                this.populateFields(rootStep);
            } catch (FileNotFoundException e) {
                Globals.mm().logs().logError(e);
            }
        });
        
        JLabel l = new JLabel("Sequence");
        l.setFont(new Font("serif", Font.BOLD, 12));
        this.add(l, "wrap");
        this.add(seqTree, "growy, pushy, wrap");
        l = new JLabel("Available Step Types");
        l.setFont(new Font("serif", Font.BOLD, 12));
        this.add(l, "wrap");
        this.add(newStepsTree, "growy 20, wrap");
        this.add(settingsPanel, "cell 1 0 1 4");

        this.add(runButton, "cell 0 5 2 1, align center");
        this.add(saveButton, "cell 0 5");
        this.add(loadButton, "cell 0 5");
    }
    
    private Step compileSequenceNodes(DefaultMutableTreeNode parent) throws InstantiationException, IllegalAccessException {
        //Only the Root can be DefaultMutableTreeNode, the rest better be StepNodes
        //Recursively compile a StepNode and it's children into a step which can be passed to the acquisition engine. 
        settingsPanel.saveSettingsOfLastNode(); //Make sure to update nodes with most recently set paremeters in the settings panel
        if (parent.getAllowsChildren()) {
            List<Step> l = new ArrayList<>();
            for (int i=0; i<parent.getChildCount(); i++) {  
                l.add(compileSequenceNodes((StepNode) parent.getChildAt(i)));
            } 
            ContainerStep step = (ContainerStep) ((StepNode) parent).createStepObject();
            step.setSubSteps(l);
            return step;
        } else {
            EndpointStep step = (EndpointStep) ((StepNode) parent).createStepObject();
            return step;
        }
    }
    
    private boolean resolveFileConflicts(Step step) {
        //TODO Don't assume starting at cell 1
        //TODO doesn't account for planned ability to change subdir.
        //returns true if it is ok ot proceed, false if cancel.
        String dir = ((SequencerSettings.RootStepSettings) step.getSettings()).directory;
        Integer numberAcqsExpected = (int) Math.ceil(step.numberNewAcqs()); //This can possible be fractional due to the `EveryNTimes` step. always round up to be safe.
        List<Path> conflict = new ArrayList<>();
        for (int i=0; i<numberAcqsExpected; i++) {
            File cellFolder = FileSpecs.getCellFolderName(Paths.get(dir), i+1).toFile();
            if (cellFolder.exists()) {
                conflict.add(Paths.get(dir).relativize(cellFolder.toPath()));
            }
        }
        boolean overwrite = (new JDialog() { //OPen a dialog asking to overwrite, if true then go ahead.
            private boolean result;
            public void setVisible(boolean vis) {
                if (vis) {
                    this.setTitle("File Conflict!");
                    //this.setResizable(false);
                    this.setModal(true);
                    this.setLocationRelativeTo(this.getOwner());
                    
                    JPanel cont = new JPanel(new MigLayout("insets 0 0 0 0, fill"));
                    JTextArea textTop = new JTextArea(String.format("The following files already exist at %s:", dir));
                    textTop.setWrapStyleWord(true);
                    textTop.setLineWrap(true);
                    textTop.setOpaque(false);
                    textTop.setEditable(false);
                    textTop.setFocusable(false);
                    cont.add(textTop, "wrap, growx, span");
                    JTextArea text = new JTextArea();
                    text.setText(String.join("\n", conflict.stream().map(Object::toString).collect(Collectors.toList())));
                    text.setWrapStyleWord(true);
                    text.setLineWrap(true);
                    text.setOpaque(false);
                    text.setEditable(false);
                    JScrollPane scroll = new JScrollPane(text);
                    cont.add(scroll, "wrap, grow, span, width 15sp, height 15sp");
                    JButton okButton = new JButton("Overwrite");
                    okButton.addActionListener((evt)->{
                        result=true;
                        this.setVisible(false);
                    });
                    cont.add(okButton);
                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener((evt)->{
                        result=false;
                        this.setVisible(false);
                    });
                    cont.add(cancelButton);
                    this.setContentPane(cont);
                    this.pack();
                    this.setMinimumSize(this.getSize());
                }
                super.setVisible(vis);
            }
            public boolean getResult() {
                this.setVisible(true);
                return this.result;
            }
        }).getResult();
        
        boolean success = overwrite;
        if (overwrite) {
            boolean err = false;
            for (Path p : conflict) {
                try {
                    FileUtils.deleteDirectory(Paths.get(dir).resolve(p).toFile());
                } catch (IOException e) {
                    Globals.mm().logs().logError(e);
                    err = true;
                }
            }
            if (err) {
                Globals.mm().logs().showMessage("A file exception was detected. Please check the CoreLog for more information.");
                success = false;
            }
        }
        return success;
    }
    
    private List<String> verifySequence(Step parent, List<String> errs) {
        if (parent instanceof ContainerStep) {
            if (((ContainerStep) parent).getSubSteps().isEmpty()) {
                errs.add(String.format("%s container-node may not be empty", parent.toString()));
            }
            for (Step substep : ((ContainerStep) parent).getSubSteps()) {
                errs.addAll(verifySequence(substep, new ArrayList<String>()));
            }
        } 
        return errs;
    }
    
    private List<String> verifySequence(Step parent) {
        List<String> errs = new ArrayList<>();
        return verifySequence(parent, errs);
    }
    
    private StepNode loadNodeFromStep(Step rootStep) {
        StepFactory factory = Consts.getFactory(rootStep.getType());
        if (rootStep instanceof ContainerStep) {
            ContainerStepNode node = new ContainerStepNode(rootStep.getSettings(), factory.getType());            
            for (Step subStep : ((ContainerStep) rootStep).getSubSteps()) {
                StepNode subNode = (StepNode) loadNodeFromStep(subStep);
                node.add(subNode);
            }
            return node;
        } else if (rootStep instanceof EndpointStep) {
            return new EndpointStepNode(rootStep.getSettings(), factory.getType());
        }
        throw new RuntimeException("Should not get here.");
    }
    
    @Override
    public ContainerStep build() throws BuilderPanelException {
        try {
            return (ContainerStep) compileSequenceNodes((DefaultMutableTreeNode) seqTree.model().getRoot());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BuilderPanelException(e);
        }
    }
    
    @Override
    public void populateFields(ContainerStep rootStep) {
        ContainerStepNode rootNode = new ContainerStepNode(rootStep.getSettings(), rootStep.getType());
        for (Step subStep : rootStep.getSubSteps()) {
            rootNode.add(loadNodeFromStep(subStep));
        }
        seqTree.model().setRoot(rootNode);
    }
    
}

class SequencerRunningDlg extends JDialog {
    JTextArea statusMsg = new JTextArea();
    JLabel cellNum = new JLabel("Acquire Cell:");
    PauseButton pauseButton = new PauseButton(true);
    JButton cancelButton = new JButton("Cancel");
    AcquisitionThread acqThread;
    
    public static void main(String[] args) {
        SequencerRunningDlg dlg;
        dlg = new SequencerRunningDlg(null, "test", 
                new SequencerFunction() {
                    @Override
                    public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                        Integer id = status.newStatusMessage("Remember:");
                        for  (int i=0; i<1000; i++) {
                            Thread.sleep(1000);
                            status.updateCellNumber(i);
                            long j =  Math.round(Math.random() * 10);
                            String s = "Forget it.";
                            status.newStatusMessage(new String(new char[(int)j]).replace("\0", s));
                            status.updateStatusMessage(id, String.format("Remember:%d", i));
                        }
                        return status;
                    }
                }
        );
    }
    
    public SequencerRunningDlg(Window owner, String title, SequencerFunction rootFunc) {
        super(owner, title, Dialog.ModalityType.DOCUMENT_MODAL);
        this.setLocationRelativeTo(owner);

        statusMsg.setEditable(false);
        JScrollPane textScroll = new JScrollPane(statusMsg);
        
        JPanel contentPane = new JPanel(new MigLayout("fill"));
        this.setContentPane(contentPane);
        contentPane.add(new JLabel("Status: "), "wrap, spanx");
        contentPane.add(textScroll, "height 20sp, width 20sp, wrap, spanx");
        contentPane.add(cellNum, "wrap");
        contentPane.add(pauseButton, "gapleft push");
        contentPane.add(cancelButton, "gapright push");
        this.pack();
        
        acqThread = new AcquisitionThread(rootFunc, 1); //This starts the thread.
        
        cancelButton.addActionListener((evt)->{
            //The acquisition engine doesn't deal well with InterruptedException. Manually cancel any acquisitions before trying to cancel the thread.
            if (Globals.mm().acquisitions().isAcquisitionRunning()) {
                Globals.mm().acquisitions().haltAcquisition();
                do {
                    try {
                        Thread.sleep(100); // Wait for the acquisition to stop.
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // reset the interrupt flag.
                    }
                } while (Globals.mm().acquisitions().isAcquisitionRunning()) ;
            }
            acqThread.cancel(true);
        });
        
        this.setVisible(true); // this blocks.
    }
    
    public void updateStatus(AcquisitionStatus status) {
        this.cellNum.setText(String.format("Acquiring Cell: %d", status.currentCellNum));
        this.statusMsg.setText(String.join("\n", status.statusMsg));
    }

    class AcquisitionThread extends SwingWorker<AcquisitionStatus, AcquisitionStatus> { //TODO maybe this hsould be inner to dialog.
        SequencerFunction rootFunc;
        private final AcquisitionStatus startingStatus;
        private AcquisitionStatus currentStatus;

        public AcquisitionThread(SequencerFunction rootFunc, Integer startingCellNum) {
            this.rootFunc = rootFunc;  
            ThrowingFunction<AcquisitionStatus, Void> publishCallback = (status) -> { this.publish(status); return null; };
            ThrowingFunction<Void, Void> pauseCallback = (nullInput) -> { SequencerRunningDlg.this.pauseButton.pausePoint(); return nullInput; };
            startingStatus = new AcquisitionStatus(publishCallback, pauseCallback);
            startingStatus.currentCellNum = startingCellNum;
            currentStatus = startingStatus;
            this.execute();
        }

        @Override
        public AcquisitionStatus doInBackground() {
            try {
                AcquisitionStatus status = rootFunc.apply(this.startingStatus);
                this.currentStatus = status;
            } catch (Exception ie) {
                Globals.mm().logs().logError(ie);
                Globals.mm().logs().showError(String.format("Error in sequencer. See core log file. %s", ie.getMessage()));
            }
            SwingUtilities.invokeLater(() -> {
                finished();
            });
            return this.currentStatus;
        }

        public void finished() {
            //SequencerRunningDlg.this.dispose();
            SequencerRunningDlg.this.statusMsg.setText(statusMsg.getText() + "\nDone.");
            SequencerRunningDlg.this.cancelButton.setEnabled(false);
            SequencerRunningDlg.this.pauseButton.setEnabled(false);
        }

        @Override
        protected void process(List<AcquisitionStatus> chunks) {
            currentStatus = chunks.get(chunks.size() - 1);
            SequencerRunningDlg.this.updateStatus(currentStatus);
            //setProgress(
        }

        //public void done() This method has a bug where it can run before the thread actually exits. Use invokelater instead.
    }
}