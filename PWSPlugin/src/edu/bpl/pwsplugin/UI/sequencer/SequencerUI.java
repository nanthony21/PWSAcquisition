/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.AutoFocusUI;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.FocusLockUI;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.PositionSequenceUI;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.TimeSeriesUI;
import edu.bpl.pwsplugin.UI.sequencer.tree.CopyMoveTransferHandler;
import edu.bpl.pwsplugin.UI.sequencer.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.UI.sequencer.tree.EndpointStepNode;
import edu.bpl.pwsplugin.UI.sequencer.tree.StepNode;
import edu.bpl.pwsplugin.UI.sequencer.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.UI.settings.DynPanel;
import edu.bpl.pwsplugin.UI.settings.FluorPanel;
import edu.bpl.pwsplugin.UI.settings.PWSPanel;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import net.miginfocom.swing.MigLayout;
        

/**
 *
 * @author nick
 */
public class SequencerUI extends JPanel {
    SequenceTree seqTree = new SequenceTree();
    NewStepsTree newStepsTree = new NewStepsTree();
    SettingsPanel settingsPanel = new SettingsPanel();
    
    public SequencerUI() {
        super(new MigLayout());

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.newStepsTree.tree().addTreeSelectionListener(this.settingsPanel);
        this.seqTree.tree().addTreeSelectionListener(this.settingsPanel);
        
        this.add(seqTree);
        this.add(newStepsTree);
        this.add(settingsPanel);
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new SequencerUI());
        f.pack();
        f.setVisible(true);
    }
}


class SettingsPanel extends JPanel implements TreeSelectionListener {
    Map<Consts.Type, BuilderJPanel> panelTypeMapping = new HashMap<>();
    StepNode lastSelectedNode = null;
    
    public SettingsPanel() {
        super(new CardLayout());
               
        panelTypeMapping.put(Consts.Type.PWS, new PWSPanel());
        panelTypeMapping.put(Consts.Type.DYN, new DynPanel());
        panelTypeMapping.put(Consts.Type.FLUOR, new FluorPanel());
        panelTypeMapping.put(Consts.Type.AF, new AutoFocusUI());
        panelTypeMapping.put(Consts.Type.PFS, new FocusLockUI());
        panelTypeMapping.put(Consts.Type.POS, new PositionSequenceUI());
        panelTypeMapping.put(Consts.Type.TIME, new TimeSeriesUI());
        
        int maxH = 0;
        int maxW = 0;
        
       
        
        for (Map.Entry<Consts.Type, BuilderJPanel> e : panelTypeMapping.entrySet()) {
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
        //Make sure to save previous settings.
        if (this.lastSelectedNode != null) {
            this.lastSelectedNode.setUserObject(panelTypeMapping.get(this.lastSelectedNode.getType()).build());
        }
        
        Object node = e.getPath().getLastPathComponent();
        if (node instanceof StepNode) { // Some nodes may be default nodes used as folders. We don't want to respond to those selections.
            StepNode n = (StepNode) node;
            this.lastSelectedNode = n;
            ((CardLayout) this.getLayout()).show(this, n.getType().toString());
            BuilderJPanel panel = panelTypeMapping.get(n.getType());
            try {
                panel.populateFields(n.getUserObject());
            } catch (Exception exc) {
                Globals.mm().logs().logError(exc);
            }
        }
    }
}

class NewStepsTree extends TreeDragAndDrop {
    public NewStepsTree() {
        super(new CopyOnlyTransferHandler());
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        DefaultMutableTreeNode acquisitions = new DefaultMutableTreeNode("Acquisitions");
        acquisitions.add(new EndpointStepNode(new PWSSettings(), Consts.Type.PWS));
        acquisitions.add(new EndpointStepNode(new DynSettings(), Consts.Type.DYN));
        acquisitions.add(new EndpointStepNode(new FluorSettings(), Consts.Type.FLUOR));
        root.add(acquisitions);
        
        DefaultMutableTreeNode utility = new DefaultMutableTreeNode("Utility");
        utility.add(new StepNode(new SoftwareAutoFocusSettings(), Consts.Type.AF));
        utility.add(new StepNode(new FocusLockSettings(), Consts.Type.PFS));
//        utility.add(new StepNode(, Consts.Type.ZOFFSET));
        root.add(utility);
        
        DefaultMutableTreeNode sequences = new DefaultMutableTreeNode("Sequences");
        sequences.add(new StepNode(new AcquirePositionsSettings(), Consts.Type.POS));
        sequences.add(new StepNode(new AcquireTimeSeriesSettings(), Consts.Type.TIME));
        root.add(sequences);
        
        model.setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
    }
}

class SequenceTree extends TreeDragAndDrop {
    public SequenceTree() {
        super(new CopyMoveTransferHandler());
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sequence Root");
        
        
        model.setRoot(root);

    }
}

