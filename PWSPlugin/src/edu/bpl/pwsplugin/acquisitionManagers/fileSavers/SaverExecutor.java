/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.micromanager.data.Image;

/**
 *
 * @author nick
 */
public abstract class SaverExecutor implements ImageSaver, Callable<Void> {
    private final ExecutorService ex = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PWS_ImageIO_Saver_Thread_%d").setPriority(Thread.MAX_PRIORITY).build());
    //private Future<Void> threadFuture;
    private final LinkedBlockingQueue<Image> queue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<MetadataBase> mdQueue = new LinkedBlockingQueue<>(1);
    private boolean initialized = false;
    protected boolean configured = false; //subclasses must set this true before running.
    
    @Override
    public void beginSavingThread() {
        if (!configured) {
            throw new RuntimeException("Must configure ImageSaver before attempting to start thread,");
        }
        if (initialized) {
            throw new RuntimeException("This ImageSaver has already been run once. You must create a new one.");
        }
        initialized = true;
        //threadFuture = ex.submit(this);
        ex.submit(this);
    }
    
    @Override
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
    }
    
    @Override
    public Void call() throws Exception {
        this.runInThread();
        ex.shutdown();
        return null;
    }
    
    protected abstract void runInThread() throws Exception;
    
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
