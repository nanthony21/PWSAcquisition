/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.stepSettings;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import net.miginfocom.swing.MigLayout;
import org.micromanager.PositionList;
import org.micromanager.internal.positionlist.PositionListDlg;

/**
 *
 * @author nick
 */
public class PositionSequenceUI extends BuilderJPanel<AcquirePositionsSettings>{
    PositionListDlg dlg;
    PositionList posList;
    
    public PositionSequenceUI() {
        super(new MigLayout(), AcquirePositionsSettings.class);
         
        dlg = new PositionListDlg(core, studio, new PositionList(), acd);
         
    }
    
    public AcquirePositionsSettings build() {
        AcquirePositionsSettings settings = new AcquirePositionsSettings();
        settings
    }
    
    @Override
    public void populateFields(AcquirePositionsSettings settings) {
        
    }
}
