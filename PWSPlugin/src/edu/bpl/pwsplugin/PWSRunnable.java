package edu.bpl.pwsplugin;

import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.Studio;


public class PWSRunnable implements Runnable {
    Studio studio_;
    PWSProcessor proc_;
    
    PWSRunnable(PWSProcessor proc) {
        studio_ = proc.studio_;  
        proc_ = proc;
    }
    
    @Override
    public void run() {
        try {
            ReportingUtils.logMessage("PWS: entering runnable");
            studio_.acquisitions().setPause(true);
            proc_.acquireImages();
            studio_.acquisitions().setPause(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logError("PWS: while entering runnable");
        }
    }

}
