/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class MMDeviceException extends Exception { //Micromanager core stupidly throw generic Exceptions. We wrap them in this specific exception.
    public MMDeviceException() { super(); }
    public MMDeviceException(Throwable cause) { super(cause); }
    public MMDeviceException(String msg) { super(msg); }
}
