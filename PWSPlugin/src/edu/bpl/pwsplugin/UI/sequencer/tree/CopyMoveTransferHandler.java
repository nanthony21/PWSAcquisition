/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY_OR_MOVE;
import static javax.swing.TransferHandler.MOVE;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class CopyMoveTransferHandler extends TransferHandler {
    DataFlavor nodesFlavor;
    List<DefaultMutableTreeNode> nodesToRemove;

    public CopyMoveTransferHandler() {
        try {
            nodesFlavor = new DataFlavors.CopiedNodeDataFlavor();
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop() || !support.isDataFlavorSupported(nodesFlavor)) {
            return false; //Not A drop action, no supported
        }
        support.setShowDropLocation(true);

        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        List<CopiedMutableTreeNode> nodes;
        try {
            nodes = (List<CopiedMutableTreeNode>) support.getTransferable().getTransferData(nodesFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e);
        }       
        
        // Do not allow a drop on the drag source selections. or a child of the source selections.
        DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();
        for (CopiedMutableTreeNode node : nodes) {
            if (node.original().equals(dropNode)) {
                return false;
            }
            else if (node.original().isNodeDescendant(dropNode)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree)c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<DefaultMutableTreeNode> copies = new ArrayList<>();
            List<DefaultMutableTreeNode> toRemove = new ArrayList<>();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
            DefaultMutableTreeNode copy = new CopiedMutableTreeNode(node);
            copies.add(copy);
            toRemove.add(node);
            for(int i = 1; i < paths.length; i++) {
                DefaultMutableTreeNode next =
                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else { // sibling
                    copies.add(new CopiedMutableTreeNode(next));
                    toRemove.add(next);
                }
            }
            nodesToRemove = toRemove;
            return new Transferables.NodesTransferable(copies);
        }
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        //If this was a move (rather than a copy) then remove the old copies.
        if((action & MOVE) == MOVE) {
            JTree tree = (JTree)source;
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            // Remove nodes saved in nodesToRemove in createTransferable.
            for(int i = 0; i < nodesToRemove.size(); i++) {
                model.removeNodeFromParent(nodesToRemove.get(i));
            }
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE; //The type of actions allowed.
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        //Implements how to import a transfer (drop operation);
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        List<CopiedMutableTreeNode> nodes;
        try {
            Transferable t = support.getTransferable();
            nodes = (List<CopiedMutableTreeNode>) t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException | java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // Get drop location info.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
        JTree tree = (JTree)support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        for(int i = 0; i < nodes.size(); i++) {
            model.insertNodeInto(nodes.get(i), parent, index++);
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}