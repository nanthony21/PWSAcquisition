/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin;

import edu.bpl.pwsplugin.acquisitionManagers.AcqManager;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.settings.ImagingConfigurationSettings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.micromanager.AutofocusPlugin;
import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;

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
    AcqManager acqMan;
    boolean autoShutter;
    double autoShutterDelay;
    
    public AcqSequencer(AcqManager manager) {
        acqMan = manager;
    }
    
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
        
    public void setSettings(int timeSteps, double timeInterval, boolean useMultiplePositions, boolean autoShutter, double autoShutterDelay, boolean pws, boolean dynamics, boolean fluor, Path dir, int cellNum) {
        this.num_frames = timeSteps;
        frame_interval = timeInterval;
        this.useMultiplePositions = useMultiplePositions;
        this.autoShutter = autoShutter;
        this.autoShutterDelay = autoShutterDelay;
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
    
    private static void alignPFS() throws Exception {
        //ALIGNPFS Turns on the pfs for a few seconds and then turns it off. If it's
        //already on then just proceed.
        if (Globals.core().getProperty("TIPFSStatus", "State").equals("Off")) {
            Globals.core().setProperty("TIPFSStatus", "State", "On");
            Thread.sleep(3000);
            Globals.core().setProperty("TIPFSStatus", "State", "Off");
        }
    }
    
    private static void autoFocusThenPFS() throws Exception {
        //AUTOFOCUSTHENPFS Summary of this function goes here
        //   Detailed explanation goes here
        AutofocusPlugin afPlugin = Globals.mm().getAutofocusManager().getAutofocusMethod();
        afPlugin.fullFocus(); //This blocks until the focus is done
        Thread.sleep(3000);
        Globals.core().setProperty("TIPFSStatus", "State", "On");
        Thread.sleep(5000);
    }
    
    private static void pauseThenPFS() throws Exception {
        Thread.sleep(1000);
        Globals.core().setProperty("TIPFSStatus", "State", "On");
        Thread.sleep(3000);
    }
    
    private static Integer acquireTimeSeries(double frame_interval, int num_frames, Function<Integer, Integer> acquisitionFuncHandle, int startingCellNum, boolean autoShutoffLight) throws Exception {
        //TIMESERIES execute acquisitionFunHandle repeatedly at a specified time
        //interval. the handle must take as input the Cell number to start at. It
        //will return the number of new acquisitions that it tood.
        int numOfNewAcqs = 0;
        double lastAcqTime = 0;
        for (int k=0; k<num_frames; k++) {
            // wait for the specified frame interval before proceeding to next frame
            if (k!=0) { //No pause for the first iteration
                int count = 0;
                while ((System.currentTimeMillis() - lastAcqTime)/1000 < frame_interval) {
                    String msg = String.format("Waiting %.1f seconds before acquiring next frame", frame_interval - (System.currentTimeMillis() - lastAcqTime)/1000);
                    Globals.statusAlert().setText(msg);
                    count++;
                    Thread.sleep(500);
                }   
                if (count == 0) {
                    Globals.statusAlert().setText(String.format("Acquistion took %.1f seconds. Longer than the frame interval.", (System.currentTimeMillis() - lastAcqTime)/1000));
                }
            }
            int saveNum = startingCellNum + numOfNewAcqs;
            lastAcqTime = System.currentTimeMillis(); //Save the current time so we can figure out when to start the next acquisition.
            numOfNewAcqs += acquisitionFuncHandle.apply(saveNum);
            String msg = String.format("Finished frame %d of %d", k, num_frames);
            Globals.mm().alerts().postAlert("PWS", null, msg);
        }
        if (autoShutoffLight) {
            Globals.mm().getShutterManager().setShutter(false); //Turn off the shutter //TODO use the Illuminator for this.
        }
        return numOfNewAcqs;
    }
    
    
    private static Integer acquireFromPositionList(Function<Integer, Integer> acquisitionFuncHandle, int startingCellNum) throws Exception {
        //ACQUIREFROMPOSITIONLIST Loops through the micromanager position list and
        //runs acquisitionFuncHandle at each position.
        int numOfNewAcqs = 0;

        Globals.core().setTimeoutMs(30000); //TODO put somewhere else. set timeout to 30 seconds. Otherwise we get an error if a position move takes greater than 5 seconds. (default timeout)
        PositionList list = Globals.mm().positions().getPositionList();
        for (int posNum=0; posNum < list.getNumberOfPositions(); posNum++) {
            MultiStagePosition pos = list.getPosition(posNum);
            String label = pos.getLabel();
            Callable<Void> preMoveRoutine = ()->{return null;};
            Callable<Void> postMoveRoutine = ()->{return null;};

            if (label.contains("APFS")) { //Turn off pfs before moving. after moving run autofocus to get bakc i the right range. then enable pfs again.
                preMoveRoutine = ()->{ Globals.core().setProperty("TIPFSStatus", "State", "Off"); return null; };
                postMoveRoutine = ()->{ autoFocusThenPFS(); return null; };     
            } else if (label.contains("ZPFS")) { //Turn off pfs, move, reenable pfs. make sure to set a coordinate for z-nonpfs for this to work.
                preMoveRoutine = ()->{ Globals.core().setProperty("TIPFSStatus", "State", "Off"); return null; };     
                postMoveRoutine = ()->{ pauseThenPFS(); return null; };
            } else if (label.contains("PFS")) { //If the position name has PFS then turn on pfs for this acquisition and then turn off.
                postMoveRoutine = ()->{ alignPFS(); return null; };
            }
            preMoveRoutine.call();
            pos.goToPosition(pos, Globals.core());   //Yes, I know this is weird. It's a static method that needs a position and the core as input.
            postMoveRoutine.call();
            int save_num = startingCellNum + numOfNewAcqs;
            // Set the display message for the type of data being acquired
            String msg = String.format("Acquiring cell: %d at position: %s", save_num, label);           
            Globals.statusAlert().setText(msg);
            numOfNewAcqs = numOfNewAcqs + acquisitionFuncHandle.apply(save_num);
        }
        list.getPosition(0).goToPosition(list.getPosition(0), Globals.core());
        return numOfNewAcqs;
    }

    private Integer acquirePWSCube(Path directoryName, Integer cellNum) {
        //Acquires and saves a PWS cube returns the number of acquisitions saved: 1.    
        this.acqMan.setCellNum(cellNum);
        this.acqMan.setSavePath(directoryName.toString());
        this.acqMan.acquirePWS();
        return 1; 
    }
    
    
    private Integer acquireFluorescence(Path directoryName, Integer cellNum) {
        this.acqMan.setCellNum(cellNum);
        this.acqMan.setSavePath(directoryName.toString());
        this.acqMan.acquireFluorescence();
        return 1;
    }
        
        
    private Integer acquireDynamics(Path directoryName, Integer cellNum) {
        //Acquires and saves a PWS dynamics cube returns the number of acquisitions saved: 1.
        this.acqMan.setCellNum(cellNum);
        this.acqMan.setSavePath(directoryName.toString());
        this.acqMan.acquireDynamics();
        return 1;
    }

    private Integer autoShutter(Double delaySeconds, ThrowingFunction<Integer, Integer> acquisitionHandle, Integer startingCellNum) throws Exception {
        //AUTOSHUTTER A function that turns on the lamp, waits `delay` seconds, runs the acquisitionHandle, then turns off the lamp.
        Globals.mm().getShutterManager().setShutter(true); //Turn on the shutter
        Globals.statusAlert().setText("Delaying acquisition while lamp warms up.");
        Thread.sleep((long)(delaySeconds*1000));
        Integer numOfNewAcqs = acquisitionHandle.apply(startingCellNum);
        Globals.mm().getShutterManager().setShutter(false); //Turn off the shutter
        return numOfNewAcqs;
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
            String cellFolderName = String.format("%s%d", "Cell", cellnum+i);
            if (pwsEnabled) {
                names.add(Paths.get(cellFolderName, "PWS"));
                count++;
            }
            if (dynamicsEnabled) {
                names.add(Paths.get(cellFolderName, "Dynamics"));
                count++;
            }
            if (fluorescenceEnabled) {
                names.add(Paths.get(cellFolderName, "Fluorescence"));
                count++;
            }
        }

        //// check for any invalid parameters and handle Errors
        try {
            if (!verifyFileName(directoryName,names)) {
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
            ThrowingFunction<Integer, Integer> acquisitionHandle = (saveNum)->{return saveNum;}; //Just a placeholder to get the handle initialized.
            if (fluorescenceEnabled) {
                ThrowingFunction<Integer, Integer> fluorHandle = (saveNum)->{
                    acquireFluorescence(directoryName, saveNum);
                    return saveNum;
                };
                acquisitionHandle = acquisitionHandle.andThen(fluorHandle);
            }
            if (pwsEnabled) {
                ThrowingFunction<Integer, Integer> pwsHandle = (saveNum)->{
                    acquirePWSCube(directoryName,saveNum); //A handle to the PWS cube acquisition routine. It takes the cell number as input
                    return saveNum;
                };
                acquisitionHandle = acquisitionHandle.andThen(pwsHandle);

            }
            if (dynamicsEnabled) {
                ThrowingFunction<Integer, Integer> dynHandle = (saveNum)->{
                    acquireDynamics(directoryName, saveNum);
                    return 0;
                };
                acquisitionHandle = acquisitionHandle.andThen(dynHandle);
            }
            if (useMultiplePositions) {
                ThrowingFunction<Integer, Integer> handleSoFar = acquisitionHandle;
                acquisitionHandle = (startingCellNum)->{
                    return acquireFromPositionList(handleSoFar, startingCellNum); //Create a handle to a function that will evaluate the previous handle at each position in the micromanager position list. takes as input the initial cell number and the number of times it has been run by the parent routine.
                };
            }
            if (autoShutter) {
                ThrowingFunction<Integer, Integer> handleSoFar = acquisitionHandle;
                acquisitionHandle = (saveNum)->{
                    return autoShutter(autoShutterDelay, handleSoFar, saveNum);
                };
            }
            if (num_frames > 1) {
                ThrowingFunction<Integer, Integer> handleSoFar = acquisitionHandle;
                acquisitionHandle = (startingCellNum)->{
                    return acquireTimeSeries(frame_interval, num_frames, handleSoFar, startingCellNum, true); //A handle that will evaluate the previous handles in a timed loop.
                };
            }

            acquisitionHandle.apply(cellnum);
        } catch (Exception e) {
            Globals.mm().logs().showError(e);
        }
    }
}

@FunctionalInterface
interface ThrowingFunction<T, R> extends Function<T, R> {
    @Override
    default R apply(T t){
        try{
            return applyThrows(t);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t) throws Exception;
    
    default <V> ThrowingFunction<T, V> andThen(ThrowingFunction<? super R, ? extends V> after){
        Objects.requireNonNull(after);
        try{
             return (T t) -> after.apply(apply(t));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    default <V> ThrowingFunction<V, R> compose(ThrowingFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        try {
            return (V v) -> apply(before.apply(v));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}