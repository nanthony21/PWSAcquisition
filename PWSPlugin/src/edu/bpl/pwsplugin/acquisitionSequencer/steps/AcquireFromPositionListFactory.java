/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.Callable;
import net.miginfocom.swing.MigLayout;
import org.micromanager.AutofocusPlugin;
import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;
import org.micromanager.data.Coords;
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
        return "Perform enclosed steps at each position in the list. Position names starting with: \"ZPFS\": Disable PFS for this position then reenable. \"APFS\": Software autofocus followed by enabling PFS. \"PFS\": Enable PFS and then disable.";
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


class AcquireFromPositionList extends ContainerStep<SequencerSettings.AcquirePositionsSettings> {
    //Executes `step` at each position in the positionlist and increments the cell number each time.
    public AcquireFromPositionList() {
        super(new SequencerSettings.AcquirePositionsSettings(), Consts.Type.POS);
    }
    
    @Override
    public SequencerFunction getStepFunction() {
        PositionList list = this.getSettings().posList;
        SequencerFunction stepFunction = super.getSubstepsFunction();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception{
                Globals.core().setTimeoutMs(30000); //TODO put somewhere else. set timeout to 30 seconds. Otherwise we get an error if a position move takes greater than 5 seconds. (default timeout)
                for (int posNum=0; posNum < list.getNumberOfPositions(); posNum++) {
                    MultiStagePosition pos = list.getPosition(posNum);
                    status.coords = status.coords.copyBuilder().stagePosition(posNum).build();
                    String label = pos.getLabel();
                    status.newStatusMessage(String.format("Moving to position %s", label));
                    Callable<Void> preMoveRoutine = ()->{return null;};
                    Callable<Void> postMoveRoutine = ()->{return null;};
                    //TODO do we want to keep this undocumented naming stuff? Replace it with a more formalized Hook functionality
                    if (label.contains("APFS")) { //Turn off pfs before moving. after moving run autofocus to get back i the right range. then enable pfs again.
                        preMoveRoutine = ()->{ Globals.core().enableContinuousFocus(false); return null; };
                        postMoveRoutine = ()->{ PFSFuncs.autoFocusThenPFS(); return null; };     
                    } else if (label.contains("ZPFS")) { //Turn off pfs, move, reenable pfs. make sure to set a coordinate for z-nonpfs for this to work.
                        preMoveRoutine = ()->{ Globals.core().enableContinuousFocus(false);  return null; };     
                        postMoveRoutine = ()->{ PFSFuncs.pauseThenPFS(); return null; };
                    } else if (label.contains("PFS")) { //If the position name has PFS then turn on pfs for this acquisition and then turn off.
                        postMoveRoutine = ()->{ PFSFuncs.alignPFS(); return null; };
                    }
                    preMoveRoutine.call();
                    pos.goToPosition(pos, Globals.core());   //Yes, I know this is weird. It's a static method that needs a position and the core as input.
                    postMoveRoutine.call();
                    status = stepFunction.apply(status);
                }
                status.coords = status.coords.copyBuilder().removeAxis(Coords.STAGE_POSITION).build();
                return status;
            }
        };
    }
    
    @Override
    protected SimFn getSimulatedFunction() {
        SimFn subStepSimFn = this.getSubStepSimFunction();
        return (Step.SimulatedStatus status) -> {
            int iterations = this.settings.posList.getNumberOfPositions();
            for (int i=0; i<iterations; i++) {
                status = subStepSimFn.apply(status);
            }
            return status;
        };
    }
    
    @Override
    public List<String> validate() {
        List<String> errs = super.validate();
        if ( this.getSettings().posList.getNumberOfPositions() == 0) {
            errs.add(String.format("Position list for \"%s\" is empty.", this.toString()));
        }
        return errs;
    }
}


class PFSFuncs {
    static void alignPFS() throws Exception {
        if (Globals.core().isContinuousFocusEnabled()) {
            Globals.core().enableContinuousFocus(true); 
            Thread.sleep(3000);
            Globals.core().enableContinuousFocus(false); 
        }
    }
    
    static void autoFocusThenPFS() throws Exception {
        AutofocusPlugin afPlugin = Globals.mm().getAutofocusManager().getAutofocusMethod();
        afPlugin.fullFocus(); //This blocks until the focus is done
        Thread.sleep(2000);
        Globals.core().enableContinuousFocus(true); 
        Thread.sleep(3000);
    }
    
    static void pauseThenPFS() throws Exception {
        Thread.sleep(1000);
        Globals.core().enableContinuousFocus(true); 
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
