/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import edu.bpl.pwsplugin.UI.sequencer.stepSettings.AutoFocusUI;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.FocusLockUI;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.PositionSequenceUI;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.TimeSeriesUI;
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
import java.util.HashMap;
import java.util.Map;
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
        
        Map<Consts.Type, BuilderJPanel> m = new HashMap<>();
        m.put(Consts.Type.PWS, new PWSPanel());
        m.put(Consts.Type.DYN, new DynPanel());
        m.put(Consts.Type.FLUOR, new FluorPanel());
        m.put(Consts.Type.AF, new AutoFocusUI());
        m.put(Consts.Type.PFS, new FocusLockUI());
        m.put(Consts.Type.POS, new PositionSequenceUI());
        m.put(Consts.Type.TIME, new TimeSeriesUI());
        
        int maxH = 0;
        int maxW = 0;
        
       
        
        for (Map.Entry<Consts.Type, BuilderJPanel> e : m.entrySet()) {
            this.add(e.getValue(), e.getKey().toString());
            int h = e.getValue().getHeight();
            if (h > maxH) {
                maxH = h;
            }
            int w = e.getValue().getWidth();
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

