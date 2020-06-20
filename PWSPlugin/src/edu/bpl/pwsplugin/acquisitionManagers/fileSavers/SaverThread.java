
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import edu.bpl.pwsplugin.metadata.MetadataBase;
import org.micromanager.data.Image;

public abstract class SaverThread extends Thread implements ImageSaver {
    //A prototype for a image saver that operates in it's own thread. Ideally it saves concurrently to the acquisition to save time.
    //Each saverthread object is only good for one image, then a new instance must be created.
    @Override
    public abstract void run(); //Starts the thread and begins the saving process. Any needed arguments should have been passed to the constructor. This should have a timeout
    
    @Override
    public abstract void setMetadata(MetadataBase md); //We allow the metadata that needs to be saved to be passed after the thread has started. This is how the metadata is set.
    
    @Override
    public abstract void addImage(Image img); //A queue is used to pass images from the acquisition thread to the saving thread. This method allows us to access the thread.
    
    @Override
    public void beginSavingThread() {
        this.start();
    }
    
    @Override
    public void awaitThreadTermination() throws InterruptedException {
        this.join();
    }
}
