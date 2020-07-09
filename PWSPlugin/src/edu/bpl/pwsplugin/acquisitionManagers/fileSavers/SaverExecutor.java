/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.micromanager.data.Image;

/**
 *
 * @author nick
 */
public abstract class SaverExecutor implements ImageSaver, Callable<Void> {
    private static final ExecutorService ex = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PWS_ImageIO_Saver_Thread_%d").setPriority(Thread.MAX_PRIORITY).build());
    private static List<Future<Void>> threadFutures = new ArrayList<>();
    private final LinkedBlockingQueue<Image> queue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<MetadataBase> mdQueue = new LinkedBlockingQueue<>(1);
    private boolean initialized = false;
    protected boolean configured = false; //subclasses must set this true before running.
    private static final Timer timer;
    
    static {
        ActionListener actlist = (evt) -> {
            try {
                SaverExecutor.processRunningFutures();
            } catch (Exception e) {
                Globals.mm().logs().logError(e);
            }
        };
        
        timer = new Timer(1000, actlist); //This timer checks the results of the saver threads on a regular interval.
        timer.setRepeats(true);
        timer.start();
    }
        
    @Override
    public void beginSavingThread() throws InterruptedException, ExecutionException {
        if (!configured) {
            throw new RuntimeException("Must configure ImageSaver before attempting to start thread,");
        }
        if (initialized) {
            throw new RuntimeException("This ImageSaver has already been run once. You must create a new one.");
        }
        initialized = true;
        
        Future future = ex.submit(this);
        threadFutures.add(future); //We used to allow multiple saving threads at once, this led to terrible write speed. Better to feed all tasks to a single thread.
        
    }
    
    private static void processRunningFutures() throws InterruptedException, ExecutionException {
        int runningTasks = 0;
        List<Future<Void>> newFutures = new ArrayList<>();
        for (Future fut : threadFutures) {
            if (fut.isDone()) {
                try {
                    fut.get(); //If an exception was thrown in the thread this will cause it to be thrown here as an ExecutionException.
                } catch (ExecutionException ee) {
                    Globals.mm().logs().showError((Exception) ee.getCause());
                }
            }
            else { 
                runningTasks++; 
                newFutures.add(fut);
            } 
        }
        threadFutures = newFutures;
        Globals.mm().logs().logMessage(String.format("Number running tasks: %d", runningTasks));
    }

    @Override
    public abstract Void call() throws Exception;
    
    @Override
    public void setMetadata(MetadataBase md) {
        mdQueue.add(md);
    }
    
    @Override
    public void addImage(Image img) {
        this.queue.add(img);
    }
    
    //Methods to be used by subclasses.
    protected LinkedBlockingQueue<Image> getImageQueue() {
        return this.queue;
    }
    
    protected LinkedBlockingQueue<MetadataBase> getMetadataQueue() {
        return this.mdQueue;
    }
}
