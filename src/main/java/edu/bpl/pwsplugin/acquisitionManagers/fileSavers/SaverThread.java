package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.data.Image;

/**
 * A base class for an ImageSaver that operates in its own thread. Ideally it saves concurrently to
 * the acquisition to save time. Each saverthread object is only good for one image, then a
 * new instance must be created.
 */
public abstract class SaverThread extends Thread implements ImageSaver {
   private final BlockingQueue<Image> q_ = new LinkedBlockingQueue<>();
   protected boolean configured = false;
         //Subclass must toggle this to indicate it is safe to start the thread.

   protected BlockingQueue<Image> getQueue() { //Internal code can retrieve images with getQueue().poll(timeout, TimeUnit.SECONDS);
      return q_;
   }

   /**
    * This runs in the separate thread and begins the saving process. Any needed arguments should
    * have been passed to the `beginSavingThread` method. This should have a timeout
    */
   @Override
   public abstract void run();

   /**
    * We allow the metadata that needs to be saved to be passed after the thread has started.
    * This is how the metadata is set.
    * @param md Metadata
    */
   @Override
   public abstract void setMetadata(MetadataBase md);

   /**
    * A queue is used to pass images from the acquisition thread to the saving thread. This method
    * allows us to access the thread.
    * @param img
    */
   @Override
   public void addImage(Image img) {
      q_.add(img);
   }

   @Override
   public void beginSavingThread() {
      if (!configured) {
         throw new RuntimeException("Cannot start saving thread without first configuring.");
      }
      this.start();
   }
}
