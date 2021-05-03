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

package edu.bpl.pwsplugin.acquisitionSequencer;

import com.google.gson.Gson;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.apache.commons.lang.StringUtils;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquisitionStatus {

   //This object acts as a go-between between the UI and the acquisition thread.
   //There should be only a single instance of this created per acquisition.
   private final RuntimeSettings runTimeSettings;
   private String currentPath;
   protected Integer currentAcquisitionlNum; //The folder number we are currently acquiring.
   private final List<String> statusMsg = new ArrayList<>();
         //A string describing what is currently happening.
   private final Function<AcquisitionStatus, Void> publishCallBack;
         //This callback should link to the `publish` method of the swingworker running the acquisition thread.
   private final Function<Void, Void> pauseCallBack;
         // This callback should link to the `pausepoint` method of a pause button.
   private final SequencerCoordinate coords;
         //This keeps track of where in the sequence we are. Callbacks can use this to determine where they are being called from.

   public AcquisitionStatus(Function<AcquisitionStatus, Void> publishCallBack,
         Function<Void, Void> pauseCallBack, Step rootStep) {
      //Create a new status object
      this.publishCallBack = publishCallBack;
      this.pauseCallBack = pauseCallBack;
      runTimeSettings = new RuntimeSettings(rootStep);
      coords = new SequencerCoordinate(runTimeSettings.getUUID());
   }

   public synchronized Integer newStatusMessage(String message) {
      Integer indentation = this.coords.getTreePath().length
            - 2; //The length of the treepath controls the indentation of messages for more readable log. The rootstep doesn't log anything so a 2 length treepath should have no indentation.
      String indent = StringUtils.repeat("  ", indentation);
      this.statusMsg.add(indent + message);
      Globals.logger().logSequence(message);
      this.publish();
      return this.statusMsg.size() - 1; //This can be used as a pointer to update the message later.
   }

   public synchronized void updateStatusMessage(Integer idx,
         String msg) { //Idx is the number that was returned by `newStatusMessage`
      //Find the original indentation so we can replicate it.
      String oldMsg = this.statusMsg.get(idx);
      int index = oldMsg.indexOf(oldMsg.trim());
      String indent = StringUtils.repeat(" ", index);
      this.statusMsg.set(idx, indent + msg);
      this.publish();
   }

   public synchronized List<String> getStatusMessage() {
      return this.statusMsg;
   }

   public void allowPauseHere() {
      pauseCallBack.apply(
            null); //If the pause button was armed then block this thread until it is disarmed.
   }

   public synchronized String getSavePath() {
      return currentPath;
   }

   public synchronized void setSavePath(String path) {
      Globals.acqManager().setSavePath(path);
      currentPath = path;
   }

   public synchronized void setAcquisitionlNum(Integer num) {
      currentAcquisitionlNum = num;
      Globals.acqManager().setCellNum(num);
      this.publish();
   }

   public synchronized Integer getAcquisitionlNum() {
      return currentAcquisitionlNum;
   }

   public synchronized SequencerCoordinate coords() {
      return this.coords;
   }

   public synchronized RuntimeSettings getRuntimeSettings() {
      return this.runTimeSettings;
   }

   public static class RuntimeSettings {

      private final String dateString = new Timestamp(new Date().getTime()).toString();
      private final String uuid = UUID.randomUUID().toString();
      private final Step rootStep;

      public RuntimeSettings(Step root) {
         rootStep = root;
      }

      public String getUUID() {
         return uuid;
      }

      public Step getRootStep() {
         return rootStep;
      }

      public void saveToJson(String directory) throws IOException {
         String savePath = Paths.get(directory, "sequence.rtpwsseq").toString();
         try (FileWriter writer = new FileWriter(
               savePath)) { //Writer is automatically closed at the end of this statement.
            Gson gson = GsonUtils.getGson();
            String json = gson.toJson(this);
            writer.write(json);
         }
      }
   }

   private void publish() {
      //Send a copy of this object back to the swingworker so it can be accessed from the `process` method.
      //We really only have one instance of this class which is not really how the publish/process mechanism is designed, but it still works as a convenient way to make UI events happen in response to calling this publish method.
      publishCallBack.apply(this);
   }
}
