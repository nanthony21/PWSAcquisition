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
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;
import net.miginfocom.swing.MigLayout;

/**
 * @author nick
 */
public class PauseStep extends EndpointStep<SequencerSettings.PauseStepSettings> {

   public PauseStep() {
      super(new SequencerSettings.PauseStepSettings(), SequencerConsts.Type.PAUSE.name());
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      SequencerSettings.PauseStepSettings settings = this.settings;
      return new SequencerFunction() {
         @Override
         public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
            SwingUtilities.invokeAndWait(() -> { //I'm not sure if this is necessary
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


   @Override
   public List<String> validate() {
      return new ArrayList<>(); //No errors are really possible here.
   }
}

class PauseDlg extends JDialog {

   JTextArea messageLabel = new JTextArea(15, 60);
   JLabel timerLabel = new JLabel();
   JButton proceedButton = new JButton("Proceed");
   long startTime = System.currentTimeMillis();
   Timer timer;

   public PauseDlg(String msg) {
      super(Globals.frame(), "Acquisition Paused",
            Dialog.ModalityType.DOCUMENT_MODAL); //Setting document_modal allows us to block the thread while allowing the main window to be interacted with while this is open.
      this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      this.setLocationRelativeTo(Globals.frame());
      this.setResizable(false);

      messageLabel.setLineWrap(true);
      messageLabel.setWrapStyleWord(true);
      messageLabel.setEditable(false);
      messageLabel.setBorder(BorderFactory.createEtchedBorder());
      timerLabel.setBorder(BorderFactory.createEtchedBorder());

      JScrollPane scroll = new JScrollPane(messageLabel);
      ((DefaultCaret) messageLabel.getCaret()).setUpdatePolicy(
            DefaultCaret.NEVER_UPDATE); // this should prevent automatic scrollin got the bottom of the textarea when it updates

      JPanel p = new JPanel(new MigLayout());
      p.add(scroll, "wrap");
      p.add(timerLabel, "wrap, align center");
      p.add(proceedButton, "align center");
      this.getContentPane().add(p);

      proceedButton.addActionListener((evt) -> {
         this.dispose();
      });

      updateTime();
      timer = new Timer(1000, (evt) -> {
         updateTime();
      });
      timer.start();
      this.pack();
      messageLabel.setText(msg);
   }

   public static void main(String args[]) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         System.out.print(e);
      }
      String msg = "Prepare to image the control dish.\n\nRemove the sample from the microscope and tilt it to thoroughly remove the current ethanol dilution with a pipette tip.\n\nWait for the sample to air dry.\n\nUse a 1ml pipette to add 1.2ml of reference solution to the sample and press `ok` to proceed with imaging.\n\n\nSteps:\n1: 60%\n2: 50%\n3: 80%\n4: 95%\n5: 90%\n6: 70%\n7: 50%\n8: 70%\n9: 90%\n10: 80%\n11: 60%\n12: 95%";
      PauseDlg dlg = new PauseDlg(msg);
      dlg.setVisible(true);
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
   public void dispose() {
      timer.stop();
      super.dispose();
   }
}