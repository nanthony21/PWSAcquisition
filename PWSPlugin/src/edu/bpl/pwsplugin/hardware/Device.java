/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware;

import java.util.List;

/**
 *
 * @author nicke
 */
public interface Device {
    public boolean identify(); //Return true if the settings indicate a device that is supported by this class. //TODO use this for automatic class selection..
    
    public List<String> validate(); //Return a list of strings for every error detected in the configuration. return empty list if no errors found.  
    
    public void initialize() throws MMDeviceException; // One time initialization of device
    
    public void activate() throws MMDeviceException; //Make sure this device is ready for usage, may be run many times.
    
    public static class IDException extends Exception {
        public IDException(String s) {
            super(s);
        }
    }
}
