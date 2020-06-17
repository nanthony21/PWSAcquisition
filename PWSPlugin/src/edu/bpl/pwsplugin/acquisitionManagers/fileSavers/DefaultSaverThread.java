/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers.fileSavers;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author nicke
 */
public abstract class DefaultSaverThread extends SaverThread {
    BlockingQueue<Object> q_;
    
    public DefaultSaverThread(BlockingQueue q) {
        q_ = q;
    }
    
    @Override
    public BlockingQueue getQueue() {
        return q_;
    }
    
}
