/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import mmcorej.org.json.JSONObject;

/**
 *
 * @author LCPWS3
 */
public abstract class FluorAcqManager implements AcquisitionManager{
    @Override
    public String getFilePrefix() {
        return "fluor";
    }
    
    @Override
    public int getExpectedFrames() {
        return 1;
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        Path path = Paths.get(savePath).resolve("Cell" + String.valueOf(cellNum)).resolve("Fluorescence");
        if (Files.isDirectory(path)){
            throw new FileAlreadyExistsException("Cell " + cellNum + " fluorescence already exists.");
        } 
        return path.toString();
    }
    
    @Override
    public abstract void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, JSONObject metadata);
}
