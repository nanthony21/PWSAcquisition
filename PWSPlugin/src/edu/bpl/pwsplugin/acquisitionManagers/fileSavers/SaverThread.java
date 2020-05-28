
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import java.util.Queue;
import mmcorej.org.json.JSONObject;

public abstract class SaverThread extends Thread{
    //A prototype for a image saver that operates in it's own thread. Ideally it saves as it goes to save time.
    @Override
    public abstract void run(); //Starts the thread and begins the saving process. An needed arguments should have been passed to the constructor.
    
    public abstract void setMetadata(JSONObject md); //We allow the metadata that needs to be saved to be passed after the thread has started. This is how the metadata is set.
    public abstract Queue getQueue();
}
