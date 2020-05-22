/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import java.awt.Container;
import java.awt.Dimension;
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
    
    public AcquirePostionsUI() {
        super(new MigLayout("insets 0 0 0 0, fill"), AcquirePositionsSettings.class);    
        dlg = new PositionListDlg(Globals.mm(), new PositionList()); 
        Container pane = dlg.getContentPane(); //We create a dialog, then steal in contents and put them in our own window, kind of hacky.
        pane.setPreferredSize(new Dimension(100, pane.getHeight())); //Make it a bit slimmer.
        this.add(pane, "grow");
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
