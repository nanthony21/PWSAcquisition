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

package edu.bpl.pwsplugin.utils;

import edu.bpl.pwsplugin.Globals;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import org.micromanager.Studio;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * This logger is responsible for writing log files. It saves it's own "PWSLog", it also
 * redirects micro-managers CoreLog to be saved in custom locations. A single file will be saved to
 * during a single session of Micro-Manager. A copy of the log file will also be saved to the
 * acquisition directory when an acquisition is being run.
 *
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class PWSLogger {
   //This writer always writes to the `logPath` that SGILogger is constructed with
   private final FileWriter logWriter;
   //When an experiment is run the `setAcquisitionPath` method is used to start a secondary log file in the acquisition path.
   private FileWriter acqWriter = null;
   private static final String LS = System.lineSeparator();
   //This integer is used as a handle by MMCore to keep track of Core log files. We need to keep this handle in order to close the file.
   private final Integer mainCoreLogHandle;
   private Integer acqCoreLogHandle = null;

   public PWSLogger(Studio studio) throws Exception {
      String homeDir = System.getProperty("user.home");
      Path logDir = Paths.get(homeDir, "PwspyApps", "PWSAcquisitionLogs");
      if (!logDir.toFile().exists()) {
         logDir.toFile().mkdirs();
      }
      logWriter = new FileWriter(
            Paths.get(logDir.toString(), "PWSLog" + new Date().getTime() + ".txt").toFile());
      mainCoreLogHandle = studio.core().startSecondaryLogFile(
            Paths.get(logDir.toString(), "CoreLog" + new Date().getTime() + ".txt").toString(),
            true); // The whole reason we pass the Studio instance to the constructor here is so we can access it without a circular dependency on Globals.mm() which isn't initialized yet.
   }

   public void logMsg(String msg) { //Log a message
      logAtLevel(msg, Level.MSG);
   }

   /**
    * Convert a throwable to a String trace and log it.
    * @param e
    */
   public void logError(Throwable e) { // Log an error
      String stackTrace = PWSLogger.getStackTraceAsString(e);
      this.logError(e.toString() + " in " + Thread.currentThread().toString() + LS + stackTrace);
   }

   public void logError(String msg) {
      logAtLevel(msg, Level.ERR);
   }

   public void logDebug(String msg) { //Log a debug message
      logAtLevel(msg, Level.DBG);
   }

   public void logSequence(String msg) { //Log a message related to the sequencer status.
      logAtLevel(msg, Level.SEQUENCE);
   }

   /**
    * Decorate a line of text with other information
    * @param msg
    * @param lvl
    * @return
    */
   private String formatLine(String msg, Level lvl) {
      String datetime = LocalDateTime.now().toString();
      String message = datetime + "::" + lvl.name() + "::" + msg + LS;
      return message;
   }

   /**
    * Other functions call this to perform the actual file writing.
    * @param msg
    * @param lvl
    */
   private synchronized void logAtLevel(String msg, Level lvl) {
      String message = formatLine(msg, lvl);
      try {
         logWriter.write(message);
         logWriter.flush();
         if (acqWriter != null) {
            acqWriter.write(message);
            acqWriter.flush();
         }
         System.out.print(message);
      } catch (IOException e) {
         ReportingUtils.showError(e);
      }
   }

   /**
    * Close all files. Do this before the program exits to avoid weirdness with files.
    * @throws Exception
    */
   public synchronized void close() throws Exception {
      logWriter.close();

      if (this.mainCoreLogHandle != null) {
         Globals.mm().core().stopSecondaryLogFile(this.mainCoreLogHandle);
      }

      if (acqWriter != null) {
         this.closeAcquisition();
      }
   }

   /**
    * Close any additional log file that was created with `setAcquisitionPath`
    * @throws Exception
    */
   public synchronized void closeAcquisition() throws Exception {
      acqWriter.close();
      acqWriter = null;

      if (this.acqCoreLogHandle != null) {
         Globals.mm().core().stopSecondaryLogFile(this.acqCoreLogHandle);
      }
   }

   /**
    * Start an additional log file that saves directly to the acquisition data path.
    * @param path
    * @throws Exception
    */
   public void setAcquisitionPath(Path path) throws Exception {
      String holder = path.toString() + "\\LOGS";
      System.out.println(holder);
      File fileholder = new File(holder);
      boolean make = fileholder.mkdir();
      if (fileholder.exists()) {
         acqWriter = new FileWriter(Paths.get(holder, "PWSLog.txt").toFile());
      }
      acqCoreLogHandle = Globals.mm().core()
            .startSecondaryLogFile(Paths.get(holder, "CoreLog.txt").toString(), true);
   }

   /**
    * Convenience function to convert from a Throwable's stacktrace to a readable string.
    * @param aThrowable
    * @return
    */
   private static String getStackTraceAsString(Throwable aThrowable) {
      String result = "";
      for (StackTraceElement line : aThrowable.getStackTrace()) {
         result += "  at " + line.toString() + "\n";
      }
      Throwable cause = aThrowable.getCause();
      if (cause != null) {
         return result + "Caused by: " + cause.toString() + "\n" + getStackTraceAsString(cause);
      } else {
         return result;
      }
   }

   /**
    * The possible types of log message.
    */
   public enum Level {
      MSG,
      DBG,
      ERR,
      SEQUENCE;
   }
}

