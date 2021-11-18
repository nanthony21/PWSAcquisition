///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//

package edu.bpl.pwsplugin.acquisitionsequencer.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.disablePanel.DisabledPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.Sequencer;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFactoryManager;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerCoordinate;
import edu.bpl.pwsplugin.acquisitionsequencer.ThrowingFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.components.PauseButton;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionListener;
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
 * This is the window that appears while the sequence is running. Displays log information and
 * allows pausing/cancelling.
 *
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
class SequencerRunningDlg extends JDialog {

   JTextArea statusMsg = new JTextArea();
   JLabel cellNum = new JLabel("Acquire Cell:");
   PauseButton pauseButton = new PauseButton(true);
   JButton cancelButton = new JButton("Cancel");
   DisplayTree tree;
   AcquisitionThread acqThread;

   public SequencerRunningDlg(Window owner, String title, Sequencer sequencer) {
      super(owner, title, Dialog.ModalityType.MODELESS);
      this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); //Must close by interrupting
      this.setLocationRelativeTo(owner);
      statusMsg.setEditable(false);
      ((DefaultCaret) statusMsg.getCaret()).setUpdatePolicy(
            DefaultCaret.NEVER_UPDATE); // this should prevent automatic scrollin got the bottom of the textarea when it updates
      JScrollPane textScroll = new JScrollPane(statusMsg);
      tree = new DisplayTree(sequencer.getRootStep(), sequencer.getFactoryManager());
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
      acqThread = new AcquisitionThread(sequencer); //This starts the thread.
      cancelButton.addActionListener((evt) -> {
         acqThread.cancel(true);
         cancelButton.setText("Awaiting...");
      });

      this.setVisible(true); // this blocks while the dialog executes.
   }

   public void updateStatus(AcquisitionStatus status) {
      this.cellNum.setText(String.format("Acquiring Cell: %d", status.getAcquisitionNum()));
      this.statusMsg.setText(String.join("\n", status.getStatusMessage()));
      Step[] treePath = status.coords().getTreePath();
      if (treePath.length
            > 0) { //The only time this shouldn't be true is when we're at the root step, the beginning or the end.
         this.tree.updateCurrentCoords(status.coords()
               .copy()); //We get a copy of the coords since the original can change from another thread.
      }
   }

   class AcquisitionThread extends SwingWorker<Void, AcquisitionStatus> {
      private final Sequencer sequencer;

      public AcquisitionThread(Sequencer sequencer) {
         this.sequencer = sequencer;
         ThrowingFunction<AcquisitionStatus, Void> publishCallback = (status) -> {
            this.publish(status);
            return null;
         };
         ThrowingFunction<Void, Void> pauseCallback = (nullInput) -> {
            SequencerRunningDlg.this.pauseButton.pausePoint();
            return nullInput;
         };
         Globals.frame().setActionButtonsEnabled(false);
         sequencer.setCallbacks(publishCallback, pauseCallback);
         this.execute();
      }

      @Override
      public Void doInBackground() {
         try {
            sequencer.runSequence();
         } finally {
            SwingUtilities.invokeLater(() -> {
               finished();
            });
         }
         return null;
      }

      public void finished() {
         //SequencerRunningDlg.this.cancelButton.setEnabled(false);
         JButton btn = SequencerRunningDlg.this.cancelButton;
         btn.setText("Finish");
         for (ActionListener l : btn.getActionListeners()) {
            btn.removeActionListener(l);
         } //clear action listeners
         btn.addActionListener((evt) -> {
            SequencerRunningDlg.this.dispose();
         }); //The old cancel button now causes the dialog to close.
         Globals.frame().setActionButtonsEnabled(true);
         SequencerRunningDlg.this.pauseButton.setEnabled(false);
      }

      @Override
      protected void process(List<AcquisitionStatus> chunks) {
         //Use only the most recently published status
         AcquisitionStatus currentStatus = chunks.get(chunks.size() - 1);
         SequencerRunningDlg.this.updateStatus(currentStatus);
      }
   }
}

/**
 * A tree view that shows the current position in the sequence.
 *
 * @author LCPWS3
 */
class DisplayTree extends JPanel {
   //This tree is used to display the current status of the sequence, it is not user interactive.
   TreeDragAndDrop tree = new TreeDragAndDrop();
   // Disabled panel with a transparent glass pane to block interaction with the tree.
   DisabledPanel disPan = new DisabledPanel(tree, new Color(1, 1, 1, 1));
   private final Step rootStep;

   public DisplayTree(Step rootStep, SequencerFactoryManager sequencerFactoryManager) {
      super(new BorderLayout());
      super.add(disPan);

      this.rootStep = rootStep;

      disPan.setEnabled(false); //Disable mouse interaction with the tree.

      ((DefaultTreeModel) tree.tree().getModel()).setRoot(rootStep);
      tree.expandTree();
      tree.tree().setCellRenderer(new TreeRenderers.SequenceRunningTreeRenderer(
            sequencerFactoryManager));
   }

   public void updateCurrentCoords(SequencerCoordinate coord) {
      tree.tree().setSelectionPath(new TreePath(
            coord.getTreePath())); //Set the step that is currently running as selected. This should cause it to become highlighted.
      ((DefaultTreeModel) tree.tree().getModel()).nodeStructureChanged(
            rootStep); //This re-renders the tree which is needed for iteration numbers of each step to be up to date.
      tree.expandTree(); //calling `nodeStructureChanged` causes everything to collapse, we don't want that.
   }
}