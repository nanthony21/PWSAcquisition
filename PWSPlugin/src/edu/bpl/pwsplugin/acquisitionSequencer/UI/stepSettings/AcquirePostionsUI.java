/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import javax.swing.JButton;
import net.miginfocom.swing.MigLayout;
import org.micromanager.PositionList;
import org.micromanager.internal.positionlist.PositionListDlg;

/**
 *
 * @author nick
 */
public class AcquirePostionsUI extends BuilderJPanel<AcquirePositionsSettings>{
    PositionListDlg dlg;
    JButton showDlgButton = new JButton("Show Position List");
    //PositionList posList;
    
    public AcquirePostionsUI() {
        super(new MigLayout(), AcquirePositionsSettings.class);    
        dlg = new PositionListDlg(Globals.mm(), new PositionList());
        
        showDlgButton.addActionListener((evt)->{
            dlg.setVisible(true);
        });
        
        this.add(showDlgButton);
    }
    
    @Override
    public AcquirePositionsSettings build() {
        AcquirePositionsSettings settings = new AcquirePositionsSettings();
        settings.posList = dlg.getPositionList();
        return settings;
    }
    
    @Override
    public void populateFields(AcquirePositionsSettings settings) {
        dlg.setPositionList(settings.posList);
    }
}
