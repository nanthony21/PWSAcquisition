/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.ContainerStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.EndpointStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquirePositionsSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.FocusLockSettings;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SoftwareAutoFocusSettings;
import java.awt.Dimension;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
class NewStepsTree extends TreeDragAndDrop {
    public NewStepsTree() {
        super(new CopyOnlyTransferHandler());
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        //DefaultMutableTreeNode acquisitions = new DefaultMutableTreeNode("Acquisitions");
        //acquisitions.add(new EndpointStepNode(new AcquireCellSettings(), Consts.Type.ACQ));
        root.add(new EndpointStepNode(new AcquireCellSettings(), Consts.Type.ACQ));
        
        DefaultMutableTreeNode utility = new DefaultMutableTreeNode("Utility");
        utility.add(new EndpointStepNode(new SoftwareAutoFocusSettings(), Consts.Type.AF));
        utility.add(new ContainerStepNode(new FocusLockSettings(), Consts.Type.PFS));
        root.add(utility);
        
        DefaultMutableTreeNode sequences = new DefaultMutableTreeNode("Sequences");
        sequences.add(new ContainerStepNode(new AcquirePositionsSettings(), Consts.Type.POS));
        sequences.add(new ContainerStepNode(new AcquireTimeSeriesSettings(), Consts.Type.TIME));
        root.add(sequences);
        
        model.setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TreeRenderers.NewStepsTreeRenderer());
        
        Dimension d = new Dimension(200, 200);
        setSize(d);
        setMinimumSize(d);
    }    
}
