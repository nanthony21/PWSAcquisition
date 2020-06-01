/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factories;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Container;
import java.awt.Dimension;
import java.util.concurrent.Callable;
import net.miginfocom.swing.MigLayout;
import org.micromanager.AutofocusPlugin;
import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;
import org.micromanager.internal.positionlist.PositionListDlg;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireFromPositionListFactory extends StepFactory {
    @Override
    public Class<? extends BuilderJPanel> getUI() {
        return AcquirePostionsUI.class;
    }
    
    @Override
    public Class<? extends JsonableParam> getSettings() {
        return SequencerSettings.AcquirePositionsSettings.class;
    }
    
    @Override
    public Class<? extends Step> getStep() {
        return AcquireFromPositionList.class;
    }
    
    @Override
    public String getDescription() {
        return "Perform enclosed steps at each position in the list.";
    }
    
    @Override
    public String getName() {
        return "Multiple Positions";
    }
    
    @Override
    public Consts.Category getCategory() {
        return Consts.Category.SEQ;
    }

    @Override
    public Consts.Type getType() {
        return Consts.Type.POS;
    }
}


class AcquireFromPositionList extends ContainerStep {
    //Executes `step` at each position in the positionlist and increments the cell number each time.
    public AcquireFromPositionList() {
        super(Consts.Type.POS);
    }
    
    @Override
    public SequencerFunction getFunction() {
        PositionList list = ((SequencerSettings.AcquirePositionsSettings) this.getSettings()).posList;
        SequencerFunction stepFunction = super.getSubstepsFunction();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception{
                Globals.core().setTimeoutMs(30000); //TODO put somewhere else. set timeout to 30 seconds. Otherwise we get an error if a position move takes greater than 5 seconds. (default timeout)
                for (int posNum=0; posNum < list.getNumberOfPositions(); posNum++) {
                    MultiStagePosition pos = list.getPosition(posNum);
                    String label = pos.getLabel();
                    status.newStatusMessage(String.format("Moving to position %s", label));
                    Callable<Void> preMoveRoutine = ()->{return null;};
                    Callable<Void> postMoveRoutine = ()->{return null;};
                    //TODO do we want to keep this undocumented naming stuff? How about we document it? duh
                    if (label.contains("APFS")) { //Turn off pfs before moving. after moving run autofocus to get back i the right range. then enable pfs again.
                        preMoveRoutine = ()->{ Globals.core().setProperty("TIPFSStatus", "State", "Off"); return null; };
                        postMoveRoutine = ()->{ PFSFuncs.autoFocusThenPFS(); return null; };     
                    } else if (label.contains("ZPFS")) { //Turn off pfs, move, reenable pfs. make sure to set a coordinate for z-nonpfs for this to work.
                        preMoveRoutine = ()->{ Globals.core().setProperty("TIPFSStatus", "State", "Off"); return null; };     
                        postMoveRoutine = ()->{ PFSFuncs.pauseThenPFS(); return null; };
                    } else if (label.contains("PFS")) { //If the position name has PFS then turn on pfs for this acquisition and then turn off.
                        postMoveRoutine = ()->{ PFSFuncs.alignPFS(); return null; };
                    }
                    preMoveRoutine.call();
                    pos.goToPosition(pos, Globals.core());   //Yes, I know this is weird. It's a static method that needs a position and the core as input.
                    postMoveRoutine.call();
                    status = stepFunction.apply(status);
                }
                list.getPosition(0).goToPosition(list.getPosition(0), Globals.core());
                return status;
            }
        };
    }

    @Override
    public Double numberNewAcqs() {
        Double acqPerIteration = super.numberNewAcqsOneIteration();
        return acqPerIteration * ((SequencerSettings.AcquirePositionsSettings) this.getSettings()).posList.getNumberOfPositions();
    }
}


class PFSFuncs {
    static void alignPFS() throws Exception {
        //ALIGNPFS Turns on the pfs for a few seconds and then turns it off. If it's
        //already on then just proceed.
        if (Globals.core().getProperty("TIPFSStatus", "State").equals("Off")) {
            Globals.core().setProperty("TIPFSStatus", "State", "On");
            Thread.sleep(3000);
            Globals.core().setProperty("TIPFSStatus", "State", "Off");
        }
    }
    
    static void autoFocusThenPFS() throws Exception {
        //AUTOFOCUSTHENPFS Summary of this function goes here
        //   Detailed explanation goes here
        AutofocusPlugin afPlugin = Globals.mm().getAutofocusManager().getAutofocusMethod();
        afPlugin.fullFocus(); //This blocks until the focus is done
        Thread.sleep(3000);
        Globals.core().setProperty("TIPFSStatus", "State", "On");
        Thread.sleep(5000);
    }
    
    static void pauseThenPFS() throws Exception {
        Thread.sleep(1000);
        Globals.core().setProperty("TIPFSStatus", "State", "On");
        Thread.sleep(3000);
    }
}

class AcquirePostionsUI extends BuilderJPanel<SequencerSettings.AcquirePositionsSettings>{
    PositionListDlg dlg;
    
    public AcquirePostionsUI() {
        super(new MigLayout("insets 0 0 0 0, fill"), SequencerSettings.AcquirePositionsSettings.class);    
        dlg = new PositionListDlg(Globals.mm(), new PositionList()); 
        Container pane = dlg.getContentPane(); //We create a dialog, then steal in contents and put them in our own window, kind of hacky.
        pane.setPreferredSize(new Dimension(100, pane.getHeight())); //Make it a bit slimmer.
        this.add(pane, "grow");
    }
    
    @Override
    public SequencerSettings.AcquirePositionsSettings build() {
        SequencerSettings.AcquirePositionsSettings settings = new SequencerSettings.AcquirePositionsSettings();
        settings.posList = dlg.getPositionList();
        return settings;
    }
    
    @Override
    public void populateFields(SequencerSettings.AcquirePositionsSettings settings) {
        dlg.setPositionList(settings.posList);
    }
}
