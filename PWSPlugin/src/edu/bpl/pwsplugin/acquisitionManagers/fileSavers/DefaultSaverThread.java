/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.micromanager.data.Image;

/**
 *
 * @author nicke
 */
public abstract class DefaultSaverThread extends SaverThread {
    BlockingQueue<Image> q_ = new LinkedBlockingQueue<>();
    
    
    protected BlockingQueue getQueue() { //Internal code can retrieve images with getQueue().poll(timeout, TimeUnit.SECONDS);
        return q_;
    }
    
    @Override
    public void addImage(Image img) {
        q_.add(img);
    }
    
}
