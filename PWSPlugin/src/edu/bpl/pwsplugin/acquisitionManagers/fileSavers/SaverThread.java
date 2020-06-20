
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.data.Image;

public abstract class SaverThread extends Thread implements ImageSaver {
    //A prototype for a image saver that operates in it's own thread. Ideally it saves concurrently to the acquisition to save time.
    //Each saverthread object is only good for one image, then a new instance must be created.
    
    private BlockingQueue<Image> q_ = new LinkedBlockingQueue<>();
    protected boolean configured = false; //Subclass must toggle this to indicate it is safe to start the thread.

    protected BlockingQueue getQueue() { //Internal code can retrieve images with getQueue().poll(timeout, TimeUnit.SECONDS);
        return q_;
    }
    
    @Override
    public abstract void run(); //This runs in the separate thread and begins the saving process. Any needed arguments should have been passed to the `beginSavingThread` method. This should have a timeout
    
    @Override
    public abstract void setMetadata(MetadataBase md); //We allow the metadata that needs to be saved to be passed after the thread has started. This is how the metadata is set.
       
    @Override
    public void addImage(Image img) { //A queue is used to pass images from the acquisition thread to the saving thread. This method allows us to access the thread.
        q_.add(img);
    }
    
    @Override
    public void beginSavingThread() {
        if (!configured) {
            throw new RuntimeException("Cannot start saving thread without first configuring.");
        }
        this.start();
    }
    
    @Override
    public void awaitThreadTermination() throws InterruptedException {
        this.join();
    }
}
