/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.bpl.pwsplugin.metadata.MetadataBase;
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
    private final ExecutorService ex = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PWS_ImageIO_Saver_Thread_%d").build());
    private Future<Void> threadFuture;
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
        threadFuture = ex.submit(this);
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
    
    @Override
    public void awaitThreadTermination() throws InterruptedException, ExecutionException {
        this.threadFuture.get();
    }
    
    //Methods to be used by subclasses.
    protected LinkedBlockingQueue<Image> getImageQueue() {
        return this.queue;
    }
    
    protected LinkedBlockingQueue<MetadataBase> getMetadataQueue() {
        return this.mdQueue;
    }
}
