/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import edu.bpl.pwsplugin.UI.sequencer.tree.CopyMoveTransferHandler;
import edu.bpl.pwsplugin.UI.sequencer.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.UI.sequencer.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.UI.settings.DynPanel;
import edu.bpl.pwsplugin.UI.settings.FluorPanel;
import edu.bpl.pwsplugin.UI.settings.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class Sequencer extends JPanel {
    TreeDragAndDrop seqTree = new TreeDragAndDrop(new CopyMoveTransferHandler());
    TreeDragAndDrop newStepsTree = new TreeDragAndDrop(new CopyOnlyTransferHandler());
    JPanel settingsPanel = new SettingsPanel();
    
    public Sequencer() {
        super(new MigLayout());

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.add(seqTree);
        this.add(newStepsTree);
        this.add(settingsPanel);
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new Sequencer());
        f.pack();
        f.setVisible(true);
    }
}


class SettingsPanel extends JPanel {
    public SettingsPanel() {
        super(new CardLayout());
        
        BuilderJPanel[] panels = {new PWSPanel(), new DynPanel(), new FluorPanel()};
        String[] names = {"pws", "dyn", "fluor"};
        assert panels.length == names.length;
        
        int maxH = 0;
        int maxW = 0;
        
        for (int i=0; i<panels.length; i++) {
            this.add(panels[i], names[i]);
            int h = panels[i].getHeight();
            if (h > maxH) {
                maxH = h;
            }
            int w = panels[i].getWidth();
            if (w > maxW) {
                maxW = w;
            }
        }
        Dimension dim = new Dimension(maxW, maxH);
        this.setSize(dim);
        this.setMinimumSize(dim);
    }
}