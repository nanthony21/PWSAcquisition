/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import edu.bpl.pwsplugin.metadata.MetadataBase;
import java.util.concurrent.ExecutionException;
import org.micromanager.data.Image;

/**
 *
 * @author nick
 */
public interface ImageSaver {
    public void configure(String savePath, String fileNamePrefix, Integer expectedFrames); //This must be run before `beginSavingThread`
    public void beginSavingThread() throws InterruptedException, ExecutionException;
    public void setMetadata(MetadataBase md);
    public void addImage(Image img);
}
