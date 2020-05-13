/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AutoshutterSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireCell;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireFromPositionList;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireTimeSeries;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AutoShutter;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.fileSpecs.FileSpecs;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

/**
 *TODO run all acquisitions through this sequencer.
 * @author nick
 */
public class AcquisitionSequencer {
    int num_frames;
    double frame_interval;
    boolean useMultiplePositions;
    PWSSettings pws;
    List<FluorSettings> fluorescence;
    DynSettings dynamics;
    Path directoryName;
    int cellnum;
    boolean autoShutter;
    double autoShutterDelay;
    
    public AcquisitionSequencer() {}
    
    private static boolean validateConfigurations() {
        //Validate all imaging configurations.
        List<String> errs = new ArrayList<>();
        for (ImagingConfigurationSettings settings : Globals.getHardwareConfiguration().getSettings().configs) {
            ImagingConfiguration conf = Globals.getHardwareConfiguration().getImagingConfigurationByName(settings.name);
            errs.addAll(conf.validate());
        }
        if (errs.size() > 0) {
            String errStr = errs.stream().collect(Collectors.joining("\n"));
            Globals.mm().logs().showMessage("Errors!\n" + errStr);
            return false;
        }
        return true;
    }
        
    public void setSettings(int timeSteps, double timeInterval, boolean useMultiplePositions, 
            boolean autoShutter, double autoShutterDelay, PWSSettings pws, DynSettings dynamics, 
            List<FluorSettings> fluor, Path dir, int cellNum) {
        this.num_frames = timeSteps;
        frame_interval = timeInterval;
        this.useMultiplePositions = useMultiplePositions;
        this.autoShutter = autoShutter;
        this.autoShutterDelay = autoShutterDelay;
        this.pws = pws;
        this.dynamics = dynamics;
        fluorescence = fluor;
        directoryName = dir;
        cellnum = cellNum;
    }
    
    public static boolean verifyFileName(List<Path> fileNames) throws FileNotFoundException, IOException {
        List<Path> conflictingNames = new ArrayList<>();
        for (Path path : fileNames) {
            if (!Files.isDirectory(path)) {
                conflictingNames.add(path);
            }
        }

        if (conflictingNames.size() > 0) {
            int option = JOptionPane.showConfirmDialog(Globals.frame(), String.valueOf(conflictingNames.size()) + " files already exist. Replace?", "Overwrite?", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                for (Path path : fileNames) {
                    Files.delete(path);
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

        List<Path> names = new ArrayList<>();
        for (int i=0; i<numAcquisitions; i++) {
            Path cellFolder = FileSpecs.getCellFolderName(directoryName, cellnum+i);
            if (pws != null) {
                String subFolder = FileSpecs.getSubfolderName(FileSpecs.Type.PWS);
                names.add(cellFolder.resolve(subFolder));
            }
            if (dynamics != null) {
                String subFolder = FileSpecs.getSubfolderName(FileSpecs.Type.DYNAMICS);
                names.add(cellFolder.resolve(subFolder));
            }
            if (fluorescence != null || fluorescence.isEmpty()) {
                String subFolder = FileSpecs.getSubfolderName(FileSpecs.Type.FLUORESCENCE);
                names.add(cellFolder.resolve(subFolder));
            }
        }

        //// check for any invalid parameters and handle Errors
        try {
            if (!verifyFileName(names)) {
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
            Step acquisitionHandle = new AcquireCell(new AcquireCellSettings());
            if (useMultiplePositions) {
                Step handleSoFar = acquisitionHandle;
                acquisitionHandle = new AcquireFromPositionList(handleSoFar, new AcquirePositionsSettings());
            }
            if (autoShutter) {
                Step handleSoFar = acquisitionHandle;
                acquisitionHandle = new AutoShutter(new AutoshutterSettings(), handleSoFar);
            }
            if (num_frames > 1) {
                Step handleSoFar = acquisitionHandle;
                acquisitionHandle = new AcquireTimeSeries(new AcquireTimeSeriesSettings(), handleSoFar);
            }

            acquisitionHandle.getFunction().apply(cellnum);
        } catch (Exception e) {
            Globals.mm().logs().showError(e);
        }
    }
}

