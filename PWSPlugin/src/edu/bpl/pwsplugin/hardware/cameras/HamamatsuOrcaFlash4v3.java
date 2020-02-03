/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.cameras;

/**
 *
 * @author N2-LiveCell
 */
public class HamamatsuOrcaFlash4v3 extends Camera{
    
    @Override
    public boolean supportsExternalTriggering() { return true; }
    
    @Override
    public void configureExternalTriggering(boolean enable); //Turn external triggering on or off.
    
    @Override
    public boolean supportsTriggerOutput() { return true; }
    
    @Override
    public void configureTriggerOutput(boolean enable); //Turn transmission of TTL pulses on or off.
    
    @Override
    public String getName(); //Get the device name used in Micro-Manager.
}
