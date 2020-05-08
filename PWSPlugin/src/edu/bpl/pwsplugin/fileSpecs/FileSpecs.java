/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.fileSpecs;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author nick
 */

public class FileSpecs {
    public enum Type {
        DYNAMICS,
        PWS,
        FLUORESCENCE
    }
    
    public static String getFilePrefix(Type type) {
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
    
    public static String getSubfolderName(Type type) {
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
    
    public static Path getCellFolderName(Path dir, int cellNum) {
        return dir.resolve(String.format("Cell%d", cellNum));
    }
}
