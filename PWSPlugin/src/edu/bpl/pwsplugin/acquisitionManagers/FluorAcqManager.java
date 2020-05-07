/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author LCPWS3
 */
abstract class FluorAcqManager implements AcquisitionManager{
    protected FluorSettings settings;
    
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
    public abstract void acquireImages(String savePath, int cellNum, LinkedBlockingQueue imagequeue, MetadataBase metadata);
    
    public void setFluorescenceSettings(FluorSettings settings) {
        this.settings = settings;
    }
}
