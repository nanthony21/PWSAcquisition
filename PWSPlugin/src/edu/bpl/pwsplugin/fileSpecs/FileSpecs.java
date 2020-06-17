/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.fileSpecs;

import java.nio.file.Path;

/**
 *
 * @author nick
 */

public class FileSpecs {
    public enum Type { // The types of acquisitions that are supported.
        DYNAMICS,
        PWS,
        FLUORESCENCE
    }
    
    public static String getFilePrefix(Type type) { //Files saved by this acquisition should be renamed to this prefix for easier identification.
        switch (type) {
            case DYNAMICS:
                return "dyn";
            case FLUORESCENCE:
                return "fluor";
            case PWS:
                return "pws";
        }
        throw new RuntimeException("Programming Error in getFilePrefix"); //If we get this far we forgot to handle a case.
    }
    
    public static String getSubfolderName(Type type) { //Files saved by this acquisition should be placed into a subfolder of the "CellX" folder by this name.
        switch (type) {
            case DYNAMICS:
                return "Dynamics";
            case FLUORESCENCE:
                return "Fluorescence";
            case PWS:
                return "PWS";
        }
        throw new RuntimeException("Programming Error in getSubfolderName"); //If we get this far we forgot to handle a case.
    }
    
    public static Path getCellFolderName(Path dir, int cellNum) { //Utility function to get the path to a main "CellX" folder.
        return dir.resolve(String.format("Cell%d", cellNum));
    }
}
