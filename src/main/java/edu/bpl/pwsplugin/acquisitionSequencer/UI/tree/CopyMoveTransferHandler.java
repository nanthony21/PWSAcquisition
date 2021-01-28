///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

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

/**
A Transfer handler that allows nodes to be moved around and well as to be copied
*/
public class CopyMoveTransferHandler extends TransferHandler {
    DataFlavor nodesFlavor;
    List<CopyableMutableTreeNode> nodesToRemove;

    public CopyMoveTransferHandler() {
        nodesFlavor = DataFlavors.CopiedNodeDataFlavor;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop() || !support.isDataFlavorSupported(nodesFlavor)) {
            return false; //Not A drop action, not supported
        }
        support.setShowDropLocation(true);

        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        List<CopyableMutableTreeNode> nodes; // The tree nodes that are being dropped.
        try {
            nodes = (List<CopyableMutableTreeNode>) support.getTransferable().getTransferData(nodesFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e);
        }       
                
        DefaultMutableTreeNode dropNode; // The node at the end of the treepath for the requested drop location.
        try {
            dropNode = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();
        } catch (NullPointerException e) {
            dropNode = (DefaultMutableTreeNode) tree.getModel().getRoot();// In some cases the path can be null. Treat this as though dropping to the root.
        }
        
        //If the node we are trying to drop into doesn't allow children then we have a problem.
        if (!dropNode.getAllowsChildren()) {
            return true;
        }
        
        // Do not allow a drop on the drag source selections or a child of the source selections since that wouldn't make any sense.
        for (CopyableMutableTreeNode node : nodes) {
            if (node.equals(dropNode)) {
                return false;
            }
            else if (node.isNodeDescendant(dropNode)) {
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
            List<CopyableMutableTreeNode> copies = new ArrayList<>();
            if (!(paths[0].getLastPathComponent() instanceof CopyableMutableTreeNode)) {
                return null; //We can't work with non copyable nodes.
            }
            CopyableMutableTreeNode node = (CopyableMutableTreeNode) paths[0].getLastPathComponent();
            copies.add(node);
            for(int i = 1; i < paths.length; i++) {
                CopyableMutableTreeNode next = (CopyableMutableTreeNode) paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else { // sibling
                    copies.add(next);
                }
            }
            nodesToRemove = copies;
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
        /*if(!canImport(support)) { //This is already handled by the API, no need to call it here.
            return false;
        }*/
        // Extract transfer data.
        List<CopyableMutableTreeNode> nodes;
        try {
            Transferable t = support.getTransferable();
            nodes = (List<CopyableMutableTreeNode>) t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException | java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // Get drop location info.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        if (dest == null) { //In cases where there is only the root node this can happen. Just treat it like the destination is the root
            dest = new TreePath(((DefaultMutableTreeNode) tree.getModel().getRoot()).getPath()[0]);
        }
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
        
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        for(int i = 0; i < nodes.size(); i++) {
            model.insertNodeInto(nodes.get(i).copy(), parent, index++);
        }
        tree.expandPath(new TreePath(parent.getPath())); //Expand the path that was just copied to.
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}