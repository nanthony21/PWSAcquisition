/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import java.util.List;


/**
 *
 * @author N2-LiveCell
 */
public abstract class TunableFilter {
    public abstract void setWavelength(int wavelength) throws Exception;
    public abstract int getWavelength() throws Exception;
    public abstract boolean supportsSequencing();
    public abstract int getMaxSequenceLength() throws Exception;
    public abstract void loadSequence(int[] wavelengthSequence) throws Exception;
    public abstract void startSequence() throws Exception;
    public abstract void stopSequence() throws Exception;
    public abstract boolean isBusy() throws Exception;
    public abstract double getDelayMs() throws Exception;
    public abstract List<String> validate(); //Return a list of errors found with the device.
    
    public static TunableFilter getInstance(Types type) {
        if (type == Types.VARISPECLCTF) {
            return new VarispecLCTF();
        } else if (type == Types.KURIOSLCTF) {
            return new KuriosLCTF();
        } else {
            return null; //This shouldn't ever happen.
        }
    }
    
    public enum Types {
        VARISPECLCTF,
        KURIOSLCTF;
    }
}
