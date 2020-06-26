/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.data.Image;

/**
 *
 * @author nick
 */
public abstract class SaverExecutor implements ImageSaver, Callable<Void> {
    private static final ExecutorService ex = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PWS_ImageIO_Saver_Thread_%d").setPriority(Thread.MAX_PRIORITY).build());
    private static List<Future<Void>> threadFutures = new ArrayList<>(); //TODO make sure to let all futures complete before exiting.
    private final LinkedBlockingQueue<Image> queue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<MetadataBase> mdQueue = new LinkedBlockingQueue<>(1);
    private boolean initialized = false;
    protected boolean configured = false; //subclasses must set this true before running.
    
    @Override
    public void beginSavingThread() throws InterruptedException, ExecutionException {
        if (!configured) {
            throw new RuntimeException("Must configure ImageSaver before attempting to start thread,");
        }
        if (initialized) {
            throw new RuntimeException("This ImageSaver has already been run once. You must create a new one.");
        }
        initialized = true;
        int todo = 0;
        for (Iterator<Future<Void>> it = threadFutures.iterator(); it.hasNext();) { //Using "for-each" looping in this case leads to a "ConcurrentModificationException"
            Future fut = it.next();
            if (fut.isDone()) {
                try {
                    fut.get(); //If an exception was thrown in the thread this will cause it to be thrown here as an ExecutionException.
                } finally {
                    it.remove();
                }
            }
            else { todo++; }
        }
        Globals.mm().logs().logMessage(String.format("TODO: %d", todo));
        Future future = ex.submit(this);
        threadFutures.add(future); //We used to allow multiple saving threads at once, this led to terrible write speed. Better to feed all tasks to a single thread.
        
    }
    
    /*@Override
    public void awaitThreadTermination() {
        ex.shutdown(); // Disable new tasks from being submitted
        try {
          // Wait a while for existing tasks to terminate
          if (!ex.awaitTermination(60, TimeUnit.SECONDS)) {
            ex.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!ex.awaitTermination(60, TimeUnit.SECONDS))
                Globals.mm().logs().logError("SaverExecutor thread did not terminate");
          }
        } catch (InterruptedException ie) {
          // (Re-)Cancel if current thread also interrupted
          ex.shutdownNow();
          // Preserve interrupt status
          Thread.currentThread().interrupt();
        }
    }*/
    
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
    
//    @Override
//    public void awaitThreadTermination() throws InterruptedException, ExecutionException {
//        this.threadFuture.get();
//    }
    
    //Methods to be used by subclasses.
    protected LinkedBlockingQueue<Image> getImageQueue() {
        return this.queue;
    }
    
    protected LinkedBlockingQueue<MetadataBase> getMetadataQueue() {
        return this.mdQueue;
    }
}
