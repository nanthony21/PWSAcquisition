/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 */
public class PWSLogger {
    private final FileWriter logWriter; //This writer always writes to the `logPath` that SGILogger is constructed with
    private FileWriter acqWriter = null; //When an experiment is run the `setAcquisitionPath` method is used to start a secondary log file in the acquisition path.
    private static final String LS = System.lineSeparator();
    private final Integer mainCoreLogHandle; //This integer is used as a handle by MMCore to keep track of Core log files. We need to keep this handle in order to close the file.
    private Integer acqCoreLogHandle = null;
    
    public PWSLogger(Studio studio) throws Exception {
        //logPath should be the directory that the log and error files will be saved to.
        //A single file will be saved to during a single session of Micro-Manager.
        //A copy of the log file will also be saved to the acquisition directory when
        //an acquisition is being run.
        String homeDir = System.getProperty("user.home");
        Path logDir = Paths.get(homeDir, "PWSAcquisitionLogs");
        if (!logDir.toFile().exists()) {
            logDir.toFile().mkdirs();
        }
        logWriter = new FileWriter(Paths.get(logDir.toString(), "PWSLog" + new Date().getTime() + ".txt").toFile());
        mainCoreLogHandle = studio.core().startSecondaryLogFile(Paths.get(logDir.toString(), "CoreLog" + new Date().getTime() + ".txt").toString(), true); // The whole reason we pass the Studio instance to the constructor here is so we can access it without a circular dependency on Globals.mm() which isn't initialized yet.
    }
    
    public void logMsg(String msg) {
        logAtLevel(msg, Level.MSG);
    }
    
    public void logError(Throwable e) {
        //Convert a throwable to a String trace and log it.
        String stackTrace = PWSLogger.getStackTraceAsString(e);
        this.logError(e.toString() + " in " + Thread.currentThread().toString() + LS + stackTrace);
    }
    
    public void logError(String msg) {  
        logAtLevel(msg, Level.ERR);
    }
    
    public void logDebug(String msg) {
        logAtLevel(msg, Level.DBG);
    }
    
    public void logSequence(String msg) {
        logAtLevel(msg, Level.SEQUENCE);
    }
    
    private synchronized void logAtLevel(String msg, Level lvl) {
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
    
    private String formatLine(String msg, Level lvl) {
        String datetime = LocalDateTime.now().toString();
        String message = datetime + "::" + lvl.name() + "::" + msg + LS;
        return message;
    }
    
    public enum Level {
        MSG,
        DBG,
        ERR,
        SEQUENCE;
    }
}

