/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;

/**
 *
 * @author Nick
 */
public class PWSFactory implements ProcessorFactory{
    
    private final Studio studio_;
    private final PropertyMap settings_;

    public PWSFactory(Studio studio, PropertyMap settings) {
        studio_ = studio;
        settings_ = settings;
    }
    @Override
    public Processor createProcessor() {
        return new PWSProcessor(studio_, settings_.getInt("numAverages"));
    }
}
