/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.AcquireFromPositionList;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Container;
import java.awt.Dimension;
import net.miginfocom.swing.MigLayout;
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
        return "Perform enclosed steps at each position in the list. Position names starting with: \"ZPFS\": Disable PFS for this position then reenable. \"APFS\": Software autofocus followed by enabling PFS. \"PFS\": Enable PFS and then disable.";
    }
    
    @Override
    public String getName() {
        return "Multiple Positions";
    }
    
    @Override
    public SequencerConsts.Category getCategory() {
        return SequencerConsts.Category.SEQ;
    }

    @Override
    public SequencerConsts.Type getType() {
        return SequencerConsts.Type.POS;
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
