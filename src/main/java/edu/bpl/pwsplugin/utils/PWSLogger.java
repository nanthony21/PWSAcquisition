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
 *
 * @author nick
 * This logger is responsible for writing log files. I saves it's own "PWSLog", it also redirects micro-managers CoreLog to be saved in custom locations.
 * A single file will be saved to during a single session of Micro-Manager.
    A copy of the log file will also be saved to the acquisition directory when
    an acquisition is being run.
 */
public class PWSLogger {
    private final FileWriter logWriter; //This writer always writes to the `logPath` that SGILogger is constructed with
    private FileWriter acqWriter = null; //When an experiment is run the `setAcquisitionPath` method is used to start a secondary log file in the acquisition path.
    private static final String LS = System.lineSeparator();
    private final Integer mainCoreLogHandle; //This integer is used as a handle by MMCore to keep track of Core log files. We need to keep this handle in order to close the file.
    private Integer acqCoreLogHandle = null;
    
    public PWSLogger(Studio studio) throws Exception {
        String homeDir = System.getProperty("user.home");
        Path logDir = Paths.get(homeDir, "PwspyApps", "PWSAcquisitionLogs");
        if (!logDir.toFile().exists()) {
            logDir.toFile().mkdirs();
        }
        logWriter = new FileWriter(Paths.get(logDir.toString(), "PWSLog" + new Date().getTime() + ".txt").toFile());
        mainCoreLogHandle = studio.core().startSecondaryLogFile(Paths.get(logDir.toString(), "CoreLog" + new Date().getTime() + ".txt").toString(), true); // The whole reason we pass the Studio instance to the constructor here is so we can access it without a circular dependency on Globals.mm() which isn't initialized yet.
    }
    
    public void logMsg(String msg) { //Log a message
        logAtLevel(msg, Level.MSG);
    }
    
    public void logError(Throwable e) { // Log an error
        //Convert a throwable to a String trace and log it.
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
    
    private String formatLine(String msg, Level lvl) { //Decorate a line of text with other information
        String datetime = LocalDateTime.now().toString();
        String message = datetime + "::" + lvl.name() + "::" + msg + LS;
        return message;
    }
    
    
    private synchronized void logAtLevel(String msg, Level lvl) { //Other functions call this to perform the actual file writing.
        String message = formatLine(msg, lvl);
        try {
            logWriter.write(message);
            logWriter.flush();
            if (acqWriter!=null) {
                acqWriter.write(message);
                acqWriter.flush();
            }
            System.out.print(message);
        } catch (IOException e) {
            ReportingUtils.showError(e);
        }
    }
    
    public synchronized void close() throws Exception {
        //Close all files. Do this before the program exits to avoid weirdness with files.
        logWriter.close();
        
        if (this.mainCoreLogHandle != null) {
            Globals.mm().core().stopSecondaryLogFile(this.mainCoreLogHandle);
        }
                
        if (acqWriter != null) {
            this.closeAcquisition();
        }     
    }
    
    public synchronized void closeAcquisition() throws Exception {
        //Close any additional log file that was created with `setAcquisitionPath`
        acqWriter.close();
        acqWriter = null;
        
        if (this.acqCoreLogHandle != null) {
            Globals.mm().core().stopSecondaryLogFile(this.acqCoreLogHandle);
        }
    }
    
    public void setAcquisitionPath(Path path) throws Exception {
        //Start an additional log file that saves directly to the acquisition data path.
        String holder = path.toString() +"\\LOGS";
        System.out.println(holder);
        File fileholder = new File(holder);
        boolean make = fileholder.mkdir();
        if(fileholder.exists()){
           acqWriter = new FileWriter(Paths.get(holder, "PWSLog.txt").toFile());
        }
        acqCoreLogHandle = Globals.mm().core().startSecondaryLogFile(Paths.get(holder, "CoreLog.txt").toString(), true);
    }
    
    private static String getStackTraceAsString(Throwable aThrowable) {
        //Convenience function to convert from a Throwable's stacktrace to a readable string.
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
    

    public enum Level { //The possible types of log message.
        MSG,
        DBG,
        ERR,
        SEQUENCE;
    }
}

