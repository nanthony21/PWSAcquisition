/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquireCellUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.SoftwareAutoFocusUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.FocusLockUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.AcquirePostionsUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.stepSettings.TimeSeriesUI;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyMoveTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.EndpointStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.StepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
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
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
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
    JButton runButton = new JButton("Run");
    Step compiledStep; //TODO just a placeholder until we know what to do with the compiled step.
    
    public SequencerUI() {
        super(new MigLayout());

        this.settingsPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.runButton.addActionListener((evt)->{ 
            compiledStep = SequencerUI.compileSequenceNodes((DefaultMutableTreeNode)seqTree.model().getRoot()); 
        }); //Run starting at cell 1.
        
        this.add(seqTree);
        this.add(newStepsTree);
        this.add(settingsPanel, "wrap");
        this.add(runButton);
    }
    
    private static Step compileSequenceNodes(DefaultMutableTreeNode parent) {
        //Only the Root can be DefaultMutableTreeNode, the rest better be StepNodes
        Step subStep = null;
        for (int i=0; i<parent.getChildCount(); i++) {
            Step compileSequenceNodes((StepNode) parent.getChildAt(i));
            //TODO build substep here.
        }
        if (parent instanceof StepNode) {
            Step step = Consts.getStepObject(((StepNode) parent).getType()).newInstance();
            step.setSettings(((StepNode) parent).getSettings());
            if (step instanceof ContainerStep) {
                if (subStep == null) { // If our step is a container step then we must have children for the node
                    throw new RuntimeException("Programming error");
                }
                ((ContainerStep) step).setSubStep(subStep);
            } else {
                if (subStep != null) { //If our step isn't a container then there shouldn't have been any children of the node. programming error
                    throw new RuntimeException("Programming error");
                }
            }
            return step;
        }
        throw new RuntimeException("AHDHDAAHDA");
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

        for (Consts.Type type : Consts.Type.values()) {
            try {
                panelTypeMapping.put(type, Consts.getUI(type).newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                Globals.mm().logs().logError(e);
            }
        }
        
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
            saveSettingsOfLastNode();
            updateSettingsFromNewNode((StepNode) node);
        }
    }
    
    private void saveSettingsOfLastNode() {
        if (this.lastSelectedNode != null) {
            this.lastSelectedNode.setSettings((SequencerSettings) panelTypeMapping.get(this.lastSelectedNode.getType()).build());
        } 
    }
    
    private void updateSettingsFromNewNode(StepNode node) { 
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
    
    @Override
    public void focusGained(FocusEvent evt) { // Clicking from one JTree to another one doesn't fire a TreeSelectionEvent. Force one to happen so the panel always shows the right settings
        TreePath path = ((JTree) evt.getComponent()).getSelectionPath();
        if (path == null) { return; }
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode)path.getLastPathComponent());
        if (node instanceof StepNode) {
            updateSettingsFromNewNode((StepNode) node);
        }
    }
    
    @Override
    public void focusLost(FocusEvent evt) {  //When the user clicks on any other component than one of the Trees make sure to save settings.
        saveSettingsOfLastNode();
    } 
}

class NewStepsTree extends TreeDragAndDrop {
    public NewStepsTree() {
        super(new CopyOnlyTransferHandler());
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        //DefaultMutableTreeNode acquisitions = new DefaultMutableTreeNode("Acquisitions");
        //acquisitions.add(new EndpointStepNode(new AcquireCellSettings(), Consts.Type.ACQ));
        root.add(new EndpointStepNode(new AcquireCellSettings(), Consts.Type.ACQ));
        
        DefaultMutableTreeNode utility = new DefaultMutableTreeNode("Utility");
        utility.add(new StepNode(new SoftwareAutoFocusSettings(), Consts.Type.AF));
        utility.add(new StepNode(new FocusLockSettings(), Consts.Type.PFS));
        root.add(utility);
        
        DefaultMutableTreeNode sequences = new DefaultMutableTreeNode("Sequences");
        sequences.add(new StepNode(new AcquirePositionsSettings(), Consts.Type.POS));
        sequences.add(new StepNode(new AcquireTimeSeriesSettings(), Consts.Type.TIME));
        root.add(sequences);
        
        model.setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        
        Dimension d = new Dimension(200, 200);
        setSize(d);
        setMinimumSize(d);
    }    
}

class SequenceTree extends TreeDragAndDrop {
    public SequenceTree() {
        super(new CopyMoveTransferHandler());
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sequence Root");
        
        model.setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(false);
        
        Dimension d = new Dimension(200, 200);
        setSize(d);
        setMinimumSize(d);
        
        setComponentPopupMenu(new PopupMenu());

    }
    
    class PopupMenu extends JPopupMenu {
        public PopupMenu() {
            super();
            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener((evt)->{
                for (TreePath path : tree.getSelectionPaths()) {
                    model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
                }
            });
            
            this.add(deleteItem);
        }
        
        @Override
        public void setVisible(boolean vis) {
            super.setVisible(vis); //just for a debug breakpoint
        }
    }
}

