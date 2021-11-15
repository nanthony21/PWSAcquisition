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

package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang.StringUtils;

/**
 * This object acts as a go-between between the UI and the acquisition thread.
 * There should be only a single instance of this created per acquisition.
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class AcquisitionStatus {
   private final RuntimeSettings runTimeSettings;
   private String currentPath;
   protected Integer currentAcquisitionNum; //The folder number we are currently acquiring.
   //This callback should link to the `publish` method of the swingworker running the acquisition thread.
   private final Function<Void, Void> pauseCallBack;
   // This callback should link to the `pausepoint` method of a pause button.
   private final Function<AcquisitionStatus, Void> publishCallBack;
   //A string describing what is currently happening.
   private final List<String> statusMsg = new ArrayList<>();
   //This keeps track of where in the sequence we are. Callbacks can use this to determine where they are being called from.
   private final SequencerCoordinate coords;

   /**
    * Create a new Status object
    *
    * @param publishCallBack
    * @param pauseCallBack
    * @param rootStep
    */
   public AcquisitionStatus(Function<AcquisitionStatus, Void> publishCallBack,
         Function<Void, Void> pauseCallBack, Step<?> rootStep) {
      this.publishCallBack = publishCallBack;
      this.pauseCallBack = pauseCallBack;
      runTimeSettings = new RuntimeSettings(rootStep);
      coords = new SequencerCoordinate(runTimeSettings.getUUID());
      currentAcquisitionNum = 1;
   }

   /**
    * Sends notification that a new message should be displayed to the user
    * @param message the message to display
    * @return The length of the formatted message. can be used for formatting the message later.
    */
   public synchronized Integer newStatusMessage(String message) {
      //The length of the treepath controls the indentation of messages for more readable log.
      // The rootstep doesn't log anything so a 2 length treepath should have no indentation.
      Integer indentation = this.coords.getTreePath().length - 2;
      String indent = StringUtils.repeat("  ", indentation);
      this.statusMsg.add(indent + message);
      Globals.logger().logSequence(message);
      this.publish();
      return this.statusMsg.size() - 1; //This can be used as a pointer to update the message later.
   }

   /**
    * Change an existing message.
    * @param idx Idx is the number that was returned by `newStatusMessage`
    * @param msg The new message contents
    */
   public synchronized void updateStatusMessage(Integer idx, String msg) {
      //Find the original indentation so we can replicate it.
      String oldMsg = this.statusMsg.get(idx);
      int index = oldMsg.indexOf(oldMsg.trim());
      String indent = StringUtils.repeat(" ", index);
      this.statusMsg.set(idx, indent + msg);
      this.publish();
   }

   /**
    *
    * @return The full string containing all status messages
    */
   public synchronized List<String> getStatusMessage() {
      return this.statusMsg;
   }

   /**
    * Call this from within the sequence thread whenever we should allow a pausecallback to execute.
    */
   public void allowPauseHere() {
      if (pauseCallBack != null) {
         //If the pause button was armed then block this thread until it is disarmed.
         pauseCallBack.apply(null);
      }
   }

   /**
    *
    * @return The current path the sequencer is set to save to.
    */
   public synchronized String getSavePath() {
      return currentPath;
   }

   /**
    * Set a new save path
    * @param path The new path
    */
   public synchronized void setSavePath(String path) {
      Globals.acqManager().setSavePath(path);
      currentPath = path;
   }

   /**
    * Set the number of acquisition that we are on
    * @param num The number.
    */
   public synchronized void setAcquisitionlNum(Integer num) {
      currentAcquisitionNum = num;
      Globals.acqManager().setCellNum(num);
      this.publish();
   }

   /**
    *
    * @return Get the currently set acquisition number.
    */
   public synchronized Integer getAcquisitionlNum() {
      return currentAcquisitionNum;
   }

   public synchronized SequencerCoordinate coords() {
      return this.coords;
   }

   public synchronized RuntimeSettings getRuntimeSettings() {
      return this.runTimeSettings;
   }

   /**
    * Execute the callback to notify a change to this object.
    */
   private void publish() {
      if (publishCallBack != null) {
         //Send a copy of this object back to the swingworker so it can be accessed from the
         // `process` method. We really only have one instance of this class which is not really
         // how the publish/process mechanism is designed, but it still works as a convenient way
         // to make UI events happen in response to calling this publish method.
         publishCallBack.apply(this);
      }
   }
}
