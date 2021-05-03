package edu.bpl.pwsplugin.acquisitionsequencer.UI;

import edu.bpl.pwsplugin.Globals;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.swing.JButton;
import org.micromanager.internal.utils.ReportingUtils;

public class PauseButton extends JButton {

   static String PAUSE = "Pause";
   static String WAIT = "Pausing...";
   static String RESUME = "Resume";
   FutureTask resumeFutureTask_;

   private boolean shouldPause() {
      //Returns true if this button is in a setting where it would like to pause when it gets a chance.
      return this.getText().equals(WAIT);
   }

   private FutureTask doNothingTask() {
      //This task is used to block the program until it is cancelled.
      return new FutureTask(() -> {
      }, null);
   }

   public boolean pausePoint() throws InterruptedException {
      //Call this method at any place in the code where you may want to be able to pause. pausing will only happend if shouldpause is true.
      //If not in the middle of MDA acquisition we can just pause our own code whenever this function is called.
      //Returns whether or not a pause actually happened.
      if (this.shouldPause()) {
         this.setText(RESUME);
         resumeFutureTask_ = doNothingTask();
         try {
            resumeFutureTask_.get();
         } catch (ExecutionException e) {
            ReportingUtils.logError(e);
         }
         return true;
      } else {
         return false;
      }
   }

   public PauseButton(boolean allowMDAPause) {
      //The allowMDAPause button determines if this button can pause in the middle of an MDA acquisition.
      super(PAUSE);

      this.addActionListener(
            (e) -> { //The following is what gets executed when the button is pressed.
               if (this.getText()
                     .equals(PAUSE)) { //Arm the button for pausing at the next opportunity
                  this.setText(WAIT);
                  if (Globals.mm().acquisitions().isAcquisitionRunning()
                        && allowMDAPause) {//Pause the MDA if we are currently running an MDA acquision
                     Globals.mm().acquisitions().setPause(true);
                     while (!Globals.mm().acquisitions().isPaused()) {
                     } //Wait for it to actually pause
                     this.setText(RESUME);
                  }
               } else if (this.getText().equals(WAIT)) { //If the button is armed then cancel.
                  this.setText(PAUSE);
               } else if (this.getText().equals(
                     RESUME)) { //If the pause button is currently pausing the program then break the pause and go back to the default mode.
                  if (Globals.mm().acquisitions().isAcquisitionRunning()
                        && allowMDAPause) { //Resume the MDA
                     Globals.mm().acquisitions().setPause(false);
                  } else { //If not in MDA we can resume by simply running our doNothingTask
                     if (resumeFutureTask_ != null) {
                        resumeFutureTask_.run();
                     }
                  }
                  this.setText(PAUSE);
               }
            });
   }
}