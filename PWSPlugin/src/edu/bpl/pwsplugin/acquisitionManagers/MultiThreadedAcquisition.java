/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.SaverThread;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.internal.utils.ReportingUtils;
import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.MMSaver;


/**
 *
 * @author nick
 */
public abstract class MultiThreadedAcquisition<S> implements Acquisition<S> {
    LinkedBlockingQueue imageQueue = new LinkedBlockingQueue();
    
    @Override
    public void acquireImages(String savePath, int cellNum, MetadataBase metadata) throws Exception {
        if (imageQueue.size() > 0) {
            ReportingUtils.showMessage(String.format("The image queue started a new acquisition with %d images already in it! Your image file is likely corrupted. This can mean that Java has not been allocated enough heap size.", imageQueue.size()));
            imageQueue.clear();
        }
        SaverThread imSaver = new MMSaver(this.getSavePath(savePath, cellNum), imageQueue, this.numFrames(), FileSpecs.getFilePrefix(this.getFileType()));
        this._acquireImages(imSaver, metadata);
    }
    
    protected abstract String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException;
    protected abstract FileSpecs.Type getFileType(); //Return the type enumerator for this acquisition, used for file saving information.
    protected abstract Integer numFrames();
    protected abstract void _acquireImages(SaverThread saver, MetadataBase md) throws Exception;

}
