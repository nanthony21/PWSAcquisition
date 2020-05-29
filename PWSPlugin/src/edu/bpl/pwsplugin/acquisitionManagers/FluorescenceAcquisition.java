/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionManagers;

import edu.bpl.pwsplugin.acquisitionManagers.fileSavers.SaverThread;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.metadata.MetadataBase;
import edu.bpl.pwsplugin.settings.FluorSettings;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author LCPWS3
 */
abstract class FluorescenceAcquisition implements Acquisition<FluorSettings>{
    protected FluorSettings settings;
    
    @Override
    public String getFilePrefix() {
        return FileSpecs.getFilePrefix(FileSpecs.Type.FLUORESCENCE);
    }
    
    @Override
    public String getSavePath(String savePath, int cellNum) throws FileAlreadyExistsException {
        int i = 0;
        Path path;
        do { //Increment the folder numbering until a nonused folder is found.
            path = FileSpecs.getCellFolderName(Paths.get(savePath), cellNum).resolve(FileSpecs.getFluorescenceSubfolderName(i));
            i++;
        } while(Files.isDirectory(path));
        return path.toString();
    }
    
    //@Override
    //public abstract void acquireImages(SaverThread saver, int cellNum, LinkedBlockingQueue imagequeue, MetadataBase metadata) throws Exception;
    
    @Override
    public void setSettings(FluorSettings settings) {
        this.settings = settings;
    }
    
    @Override
    public FluorSettings getSettings() {
        return this.settings;
    }
    
    @Override public Integer numFrames() {
        return 1;
    }
}
