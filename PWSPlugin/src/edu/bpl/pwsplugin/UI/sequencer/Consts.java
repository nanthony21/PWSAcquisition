/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

/**
 *
 * @author nick
 */
public class Consts {
    public enum Type {
        ACQ,
        PFS,
        POS,
        TIME,
        ZOFFSET,
        AF;
    }
    
    public enum Category {
        ACQ,
        SEQ,
        UTIL;
    }
    
    public static String getName(Type type) {
        switch (type) {
            case ACQ:
                return "Acquisition";
            case PFS:
                return "Optical Focus Lock";
            case POS:
                return "Multiple Positions";
            case TIME:
                return "Time Series";
            case ZOFFSET:
                return "Z Offset"; //TODO combine with focuslock?
            case AF:
                return "Software Autofocus";                  
        }
        throw new RuntimeException("Shouldn't get here");
    }
    
    public static Category getCategory(Type type) {
        if (type == Type.ACQ) {
            return Category.ACQ;
        } else if (type == Type.POS || type == Type.TIME) {
            return Category.SEQ;
        } else if (type == Type.PFS || type == Type.ZOFFSET || type == Type.AF) {
            return Category.UTIL;
        }
        throw new RuntimeException("Shouldn't get here");
    }   
}