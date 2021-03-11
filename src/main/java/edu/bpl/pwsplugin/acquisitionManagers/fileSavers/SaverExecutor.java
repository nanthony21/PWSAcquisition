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
import javax.management.RuntimeErrorException;
import java.util.Timer;
import java.util.TimerTask;
import org.micromanager.data.Image;

/**
 * Each time a new image is requested to be saved a new thread will be created to handle it.
 * TODO: The usage of static variables was probably not smart. This should be split into two classes. One is the executor that handles executing tasks on a single thread. The other class represents the task that can be submitted to an executor.
 * Rather than having two queues (images, and metadata) why not have one queue containing Image/metadata pairs.
 * @author nick
 */
public abstract class SaverExecutor implements ImageSaver, Callable<Void> {
    private static final ExecutorService ex = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PWS_ImageIO_Saver_Thread_%d").setPriority(Thread.MAX_PRIORITY).build()); // A single thread handles saving for the whole application.
    private static final List<Future<Void>> threadFutures = new ArrayList<>(); // This list keeps track of the save tasks that are still running. The timer periodically cleans the list of finished tasks.
    private final LinkedBlockingQueue<Image> imQueue = new LinkedBlockingQueue<>(); // A queue to pass images between threads.
    private final LinkedBlockingQueue<MetadataBase> mdQueue = new LinkedBlockingQueue<>(1); // A  queue to pass metadata between queues.
    private boolean initialized = false; //Subclasses of this can only be run once, this varible checks to make sure that is the case.
    protected boolean configured = false; //subclasses must set this to `true` before running.
    private static final Timer timer;
    
    static {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    SaverExecutor.processRunningFutures();
                } catch (Exception e) {
                    Globals.mm().logs().logError(e);
                }
            }
        };
        
        timer = new Timer("SaverExecutorTimerThread");
        timer.scheduleAtFixedRate(task, 1000, 1000);// 1000, actlist); //This timer checks the results of the saver threads on a regular interval. It will call its target from the AWT eventqueue thread.
    }
        
    /**
     * This will be called from the thread of whatever piece of code requests saving.
     * @throws InterruptedException
     * @throws ExecutionException 
     */
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
        
        synchronized (threadFutures) { // Make sure this list is only modified by one thread at a time.
            threadFutures.add(future); //We used to allow multiple saving threads at once, this led to terrible write speed. Better to feed all tasks to a single thread.     
        }
    }
    
    /**
     * This will be called by the timer. On the AWT_eventqueue thread.
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    private static void processRunningFutures() throws InterruptedException, ExecutionException {
        synchronized (threadFutures) { // Make sure no other thread modifies the list while we're working with it.
            Iterator<Future<Void>> iter = threadFutures.iterator();
            while (iter.hasNext()) {  // Since we are modifying the list as we loop through it we need to use iterator rather than a normal for-loop.
                Future fut = iter.next();
                if (fut.isDone()) {
                    try {
                        fut.get(); //If an exception was thrown in the thread this will cause it to be thrown here as an ExecutionException.
                    } catch (ExecutionException ee) {
                        if (ee.getCause() instanceof Exception) {
                            Globals.mm().logs().showError(ee.getCause().getMessage());
                            Globals.mm().logs().logError(ee.getCause());
                        } else {
                            throw new RuntimeErrorException((Error) ee.getCause());
                        }
                    }
                    iter.remove(); // Regardless of whether an error was thrown, this future is finished, remove it from the list.
                }
            }  
        }
    }

    @Override
    public abstract Void call() throws Exception;
    
    @Override
    public void setMetadata(MetadataBase md) {
        mdQueue.add(md);
    }
    
    @Override
    public void addImage(Image img) {
        this.imQueue.add(img);
    }
    
    //Methods to be used by subclasses.
    
    /**
     * 
     * @return A queue to be used for passing images between threads.
     */
    protected LinkedBlockingQueue<Image> getImageQueue() {
        return this.imQueue;
    }
    
    /**
     * 
     * @return A queue to be used for passing metadata between threads.
     */
    protected LinkedBlockingQueue<MetadataBase> getMetadataQueue() {
        return this.mdQueue;
    }
}
