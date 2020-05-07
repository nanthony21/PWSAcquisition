/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

/**
 *TODO run all acquisitions through this sequencer.
 * @author nick
 */
public class AcqSequencer {
    int num_frames;
    double frame_interval;
    boolean useMultiplePositions;
    boolean pwsEnabled;
    boolean fluorescenceEnabled;
    boolean dynamicsEnabled;
    Path directoryName;
    int cellnum;
    static String filePrefix = "Cell";
    static String dynamicsFolder = "Dynamics";
    static String pwsFolder = "PWS";
    static String flFolder = "Fluorescence";
    
    private static boolean validateConfigurations() {
        //Validate all imaging configurations.
        List<String> errs = new ArrayList<>();
        for (ImagingConfigurationSettings settings : Globals.getHardwareConfiguration().getSettings().configs) {
            ImagingConfiguration conf = Globals.getHardwareConfiguration().getConfigurationByName(settings.name);
            errs.addAll(conf.validate());
        }
        if (errs.size() > 0) {
            String errStr = errs.stream().collect(Collectors.joining("\n"));
            Globals.mm().logs().showMessage("Errors!\n" + errStr);
            return false;
        }
        return true;
    }
        
    public void setSettings(int timeSteps, double timeInterval, boolean useMultiplePositions, boolean pws, boolean dynamics, boolean fluor, Path dir, int cellNum) {
        this.num_frames = timeSteps;
        frame_interval = timeInterval;
        this.useMultiplePositions = useMultiplePositions;
        pwsEnabled = pws;
        dynamicsEnabled = dynamics;
        fluorescenceEnabled = fluor;
        directoryName = dir;
        cellnum = cellNum;
    }
    
    public static boolean verifyFileName(Path directoryName, List<Path> fileNames) throws FileNotFoundException, IOException {
        if (!Files.isDirectory(directoryName)) {
            throw new FileNotFoundException(directoryName.toString() + " does not exist.");
        }

        List<Path> conflictingNames = new ArrayList<>();
        for (Path path : fileNames) {
            if (!Files.isDirectory(directoryName.resolve(path))) {
                conflictingNames.add(path);
            }
        }

        if (conflictingNames.size() > 0) {
            int option = JOptionPane.showConfirmDialog(Globals.frame(), String.valueOf(conflictingNames.size()) + " files already exist. Replace?", "Overwrite?", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                for (Path path : fileNames) {
                    Files.delete(directoryName.resolve(path));
                }
                return true;
            } else {
                return false;
            }
        } else {
            return true; //No conflicts were found.
        }
        
    }
    
    public void run() {
    
    
    //Build an array of all the used filenames.
    int posNums;
    if (useMultiplePositions) {
        posNums = Globals.mm().positions().getPositionList().getNumberOfPositions();
    } else {
        posNums = 1;
    }
    int numAcquisitions = (num_frames * posNums);
    int count = 1;
    
    List<Path> names = new ArrayList<>();
    for (int i=0; i<numAcquisitions; i++) {
        String cellFolderName = String.format("%s%d",filePrefix, cellnum+i);
        if (pwsEnabled) {
            names.add(Paths.get(cellFolderName, pwsFolder));
            count++;
        }
        if (dynamicsEnabled) {
            names.add(Paths.get(cellFolderName, dynamicsFolder));
            count++;
        }
        if (fluorescenceEnabled) {
            names.add(Paths.get(cellFolderName, flFolder));
            count++;
        }
    }

    //// check for any invalid parameters and handle Errors
    try {
        if (!this.verifyFileName(directoryName,names)) {
            return;
        }
    } catch (IOException e) {
        Globals.mm().logs().showError(e);
    }
    
    if (frame_interval < 0) {
        Globals.mm().logs().showMessage("Error: Must specify valid frame interval.");
        return;
    } else if (num_frames < 1) {
        Globals.mm().logs().showMessage("Error: Must specify at least 1 frame.");
        return;
    }

    // No errors occurred. Proceed.
    try {
        List<Function<Integer, Void>> tasks = new ArrayList<>();
        Function<Integer, Integer> acquisitionHandle = (saveNum)->{return saveNum;}; //Just a placeholder to get the handle initialized.
        if (fluorescenceEnabled) {
            Function<Integer, Integer> fluorHandle = (saveNum)->{
                //subroutine.acquireFluorescence(handles, directoryName, saveNum, flExposure);
                return saveNum;
            };
            acquisitionHandle = acquisitionHandle.andThen(fluorHandle);
        }
        if (pwsEnabled) {
            Function<Integer, Integer> pwsHandle = (saveNum)->{
                //subroutine.acquirePWSCube(handles, directoryName,saveNum, pwsExposure); //A handle to the PWS cube acquisition routine. It takes the cell number as input
                return saveNum;
            };
            acquisitionHandle = acquisitionHandle.andThen(pwsHandle);
   
        }
        if (dynamicsEnabled) {
            Function<Integer, Integer> dynHandle = (saveNum)->{
                //subroutine.acquireDynamics(handles, dynamicsExposure,directoryName, saveNum);
                return 0;
            };
            acquisitionHandle = acquisitionHandle.andThen(dynHandle);
        }
        if (useMultiplePositions) {
            acquisitionHandle = (startingCellNum)->{
                subroutine.acquireFromPositionList(handles, acquisitionHandle,startingCellNum); //Create a handle to a function that will evaluate the previous handle at each position in the micromanager position list. takes as input the initial cell number and the number of times it has been run by the parent routine.
            };
        }
        if (num_frames > 1)
            acquisitionHandle = (startingCellNum)->{
                subroutine.acquireTimeSeries(handles,frame_interval,num_frames, acquisitionHandle, startingCellNum, true); //A handle that will evaluate the previous handles in a timed loop.
            };
        }
    
        acquisitionHandle.apply(cellnum);
    } catch (Exception e) {
        Globals.mm().logs().showError(e);
    }
}
