/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.sequencer.stepSettings.AcquireCellUI;
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
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.miginfocom.swing.MigLayout;
        

/**
 *
 * @author nick
 */
public class SequencerUI extends JPanel {
    SequenceTree seqTree = new SequenceTree();
    NewStepsTree newStepsTree = new NewStepsTree();
    SettingsPanel settingsPanel = new SettingsPanel(seqTree, newStepsTree);
    
    public SequencerUI() {
        super(new MigLayout());

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
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


class SettingsPanel extends JPanel implements TreeSelectionListener, FocusListener {
    Map<Consts.Type, BuilderJPanel> panelTypeMapping = new HashMap<>();
    StepNode lastSelectedNode = null;
    
    public SettingsPanel(TreeDragAndDrop... trees) {
        super(new CardLayout());
        
        //Register as a listener for the trees that we want to display settings for.
        for (int i=0; i<trees.length; i++) {
            trees[i].tree().addTreeSelectionListener(this);
            trees[i].tree().addFocusListener(this);
        }

        panelTypeMapping.put(Consts.Type.ACQ, new AcquireCellUI());
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
    public void valueChanged(TreeSelectionEvent e) { //When a new node is selected in a tree, show the settings for the node.
        Object node = e.getPath().getLastPathComponent();
        if (node instanceof StepNode) { // Some nodes may be default nodes used as folders. We don't want to respond to those selections.
            updateSettingsFromNode((StepNode) node);
        }
    }
    
    private void updateSettingsFromNode(StepNode node) {
        //Make sure to save previous settings.
        if (this.lastSelectedNode != null) {
            this.lastSelectedNode.setSettings((SequencerSettings) panelTypeMapping.get(this.lastSelectedNode.getType()).build());
        }
        
        StepNode n = (StepNode) node;
        this.lastSelectedNode = n;
        ((CardLayout) this.getLayout()).show(this, n.getType().toString());
        BuilderJPanel panel = panelTypeMapping.get(n.getType());
        try {
            panel.populateFields(n.getSettings());
        } catch (Exception exc) {
            Globals.mm().logs().logError(exc);
        }
    }
    
    @Override //TODO still a little weird
    public void focusGained(FocusEvent evt) { // Clicking from one JTree to another one doesn't fire a TreeSelectionEvent. Force one to happen so the panel always shows the right settings
        TreePath path = ((JTree) evt.getComponent()).getSelectionPath();
        if (path == null) { return; }
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode)path.getLastPathComponent());
        if (node instanceof StepNode) {
            updateSettingsFromNode((StepNode) node);
        }
    }
    
    @Override
    public void focusLost(FocusEvent evt) { } //Do nothing
}

class NewStepsTree extends TreeDragAndDrop {
    public NewStepsTree() {
        super(new CopyOnlyTransferHandler());
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        DefaultMutableTreeNode acquisitions = new DefaultMutableTreeNode("Acquisitions");
        acquisitions.add(new EndpointStepNode(new AcquireCellSettings(), Consts.Type.ACQ));
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

