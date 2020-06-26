/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.disablePanel.DisabledPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.ThrowingFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
class SequencerRunningDlg extends JDialog {
    
    JTextArea statusMsg = new JTextArea();
    JLabel cellNum = new JLabel("Acquire Cell:");
    PauseButton pauseButton = new PauseButton(true);
    JButton cancelButton = new JButton("Cancel");
    DisplayTree tree;
    AcquisitionThread acqThread;

    public SequencerRunningDlg(Window owner, String title, Step rootStep) {
        super(owner, title, Dialog.ModalityType.DOCUMENT_MODAL);
        this.setLocationRelativeTo(owner);
        statusMsg.setEditable(false);
        ((DefaultCaret) statusMsg.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE); // this should prevent automatic scrollin got the bottom of the textarea when it updates
        JScrollPane textScroll = new JScrollPane(statusMsg);
        tree = new DisplayTree(rootStep);
        JPanel contentPane = new JPanel(new MigLayout("fill"));
        this.setContentPane(contentPane);
        contentPane.add(new JLabel("Status: "), "cell 0 0");
        contentPane.add(textScroll, "cell 0 1, height 20sp, width 20sp, grow, push");
        contentPane.add(cellNum, "cell 0 2");
        contentPane.add(tree, "cell 1 0 1 3, growy");
        contentPane.add(pauseButton, "cell 0 3, gapleft push, align center");
        contentPane.add(cancelButton, "cell 0 3, gapright push, align center");
        this.pack();
        this.setResizable(true);
        acqThread = new AcquisitionThread(rootStep.getFunction(new ArrayList<>()), 1); //This starts the thread.
        cancelButton.addActionListener((evt) -> {
            //The acquisition engine doesn't deal well with InterruptedException. Manually cancel any acquisitions before trying to cancel the thread.
            if (Globals.mm().acquisitions().isAcquisitionRunning()) {
                Globals.mm().acquisitions().haltAcquisition();
                do {
                    try {
                        Thread.sleep(100); // Wait for the acquisition to stop.
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // reset the interrupt flag.
                    }
                } while (Globals.mm().acquisitions().isAcquisitionRunning());
            }
            acqThread.cancel(true);
        });
        this.setVisible(true); // this blocks.
    }

    public void updateStatus(AcquisitionStatus status) {
        this.cellNum.setText(String.format("Acquiring Cell: %d", status.getCellNum()));
        this.statusMsg.setText(String.join("\n", status.getStatusMessage()));
        Step[] treePath = status.coords().getTreePath();
        if (treePath.length > 0) { //The only time this shouldn't be true is when we're at the root step, the beginning or the end.
            this.tree.setSelectionPath(new TreePath(status.coords().getTreePath()));
        }
    }

    class AcquisitionThread extends SwingWorker<Void, AcquisitionStatus> {
        SequencerFunction rootFunc;
        private final AcquisitionStatus startingStatus;

        public AcquisitionThread(SequencerFunction rootFunc, Integer startingCellNum) {
            this.rootFunc = rootFunc;
            ThrowingFunction<AcquisitionStatus, Void> publishCallback = (status) -> {
                this.publish(status);
                return null;
            };
            ThrowingFunction<Void, Void> pauseCallback = (nullInput) -> {
                SequencerRunningDlg.this.pauseButton.pausePoint();
                return nullInput;
            };
            startingStatus = new AcquisitionStatus(publishCallback, pauseCallback);
            this.execute();
        }

        @Override
        public Void doInBackground() {
            try {
                AcquisitionStatus status = rootFunc.apply(this.startingStatus);
            } catch (Exception ie) {
                Globals.mm().logs().logError(ie);
                Globals.mm().logs().showError(String.format("Error in sequencer. See core log file. %s", ie.getMessage()));
            }
            SwingUtilities.invokeLater(() -> {
                finished();
            });
            return null;
        }

        public void finished() {
            //SequencerRunningDlg.this.cancelButton.setEnabled(false);
            JButton btn = SequencerRunningDlg.this.cancelButton;
            btn.setText("Finish");
            for (ActionListener l : btn.getActionListeners()) { btn.removeActionListener(l); } //clear action listeners
            btn.addActionListener((evt)->{ SequencerRunningDlg.this.dispose(); });
            
            SequencerRunningDlg.this.pauseButton.setEnabled(false);
        }

        @Override
        protected void process(List<AcquisitionStatus> chunks) {
            AcquisitionStatus currentStatus = chunks.get(chunks.size() - 1); //Use only the most recently published status
            SequencerRunningDlg.this.updateStatus(currentStatus);
        }
        //public void done() This method has a bug where it can run before the thread actually exits. Use invokelater instead.
    } 
}


class DisplayTree extends JPanel {
    TreeDragAndDrop tree = new TreeDragAndDrop(); //This tree is used to display the current status of the sequence, it is not user interactive.
    DisabledPanel disPan = new DisabledPanel(tree, new Color(1, 1, 1, 1)); // Disabled panel with a transparent glass pane to block interaction.
    
    public DisplayTree(Step rootStep) {
        super(new BorderLayout());
        super.add(disPan);
        
        disPan.setEnabled(false); //Disable mouse interaction with the tree.
        
        ((DefaultTreeModel) tree.tree().getModel()).setRoot(rootStep);
        tree.expandTree();
        tree.tree().setCellRenderer(new TreeRenderers.SequenceTreeRenderer() );
    }
    
    public void setSelectionPath(TreePath path) {
        tree.tree().setSelectionPath(path);
    }
}