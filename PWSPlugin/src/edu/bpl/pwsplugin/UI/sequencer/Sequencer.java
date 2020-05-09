/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import edu.bpl.pwsplugin.UI.sequencer.tree.CopyMoveTransferHandler;
import edu.bpl.pwsplugin.UI.sequencer.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.UI.sequencer.tree.StepNode;
import edu.bpl.pwsplugin.UI.sequencer.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.UI.settings.DynPanel;
import edu.bpl.pwsplugin.UI.settings.FluorPanel;
import edu.bpl.pwsplugin.UI.settings.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.settings.DynSettings;
import edu.bpl.pwsplugin.settings.FluorSettings;
import edu.bpl.pwsplugin.settings.PWSSettings;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import net.miginfocom.swing.MigLayout;
        

/**
 *
 * @author nick
 */
public class Sequencer extends JPanel {
    SequenceTree seqTree = new SequenceTree();
    NewStepsTree newStepsTree = new NewStepsTree();
    SettingsPanel settingsPanel = new SettingsPanel();
    
    public Sequencer() {
        super(new MigLayout());

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.newStepsTree.tree().addTreeSelectionListener(this.settingsPanel);
        
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


class SettingsPanel extends JPanel implements TreeSelectionListener {
    public SettingsPanel() {
        super(new CardLayout());
        
        BuilderJPanel[] panels = {new PWSPanel(), new DynPanel(), new FluorPanel()};
        Consts.Type[] names = {Consts.Type.PWS, Consts.Type.DYN, Consts.Type.FLUOR};
        assert panels.length == names.length;
        
        int maxH = 0;
        int maxW = 0;
        
        for (int i=0; i<panels.length; i++) {
            this.add(panels[i], names[i].toString());
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
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        //e.getPath()
        StepNode n = (StepNode)(e.getPath().getLastPathComponent());
        ((CardLayout) this.getLayout()).show(this, n.getType().toString());
    }
}

class NewStepsTree extends TreeDragAndDrop {
    public NewStepsTree() {
        super(new CopyOnlyTransferHandler());
        StepNode root = new StepNode(new PWSSettings(), Consts.Type.PWS);
        
        root.add(new StepNode(new DynSettings(), Consts.Type.DYN));
        root.add(new StepNode(new FluorSettings(), Consts.Type.TIME));
        //((DefaultMutableTreeNode) this.model.getRoot()).removeAllChildren();
        model.setRoot(root);
    }
}

class SequenceTree extends TreeDragAndDrop {
    public SequenceTree() {
        super(new CopyMoveTransferHandler());
        
    }
}

