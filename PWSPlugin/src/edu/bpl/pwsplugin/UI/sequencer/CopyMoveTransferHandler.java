/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY;
import static javax.swing.TransferHandler.COPY_OR_MOVE;
import static javax.swing.TransferHandler.MOVE;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

class CopyMoveTransferHandler extends TransferHandler {
    DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    List<DefaultMutableTreeNode> nodesToRemove;

    public CopyMoveTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                              "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }
    
    private void log(String msg) {
        System.out.println(msg);
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop() || !support.isDataFlavorSupported(nodesFlavor)) {
            return false; //Not A drop action, no supported
        }
        support.setShowDropLocation(true);

        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
                
        // Do not allow a drop on the drag source selections.
        List<DefaultMutableTreeNode> nodes;
        try {
            nodes = (List<DefaultMutableTreeNode>) support.getTransferable().getTransferData(nodesFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e);
        }
        DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();
        for (DefaultMutableTreeNode node : nodes) {
            if (node.equals(dropNode)) {
                log("HAHA");
                return false;
            }
        }

        // Do not allow MOVE-action drops if a non-leaf node is
        // selected unless all of its children are also selected.
        int action = support.getDropAction();
        switch (action) {
            case MOVE:
                return true;
            case COPY:
                // Do not allow a non-leaf node to be copied to a level
                // which is less than its source level.
                TreePath dest = dl.getPath();
                DefaultMutableTreeNode target =
                    (DefaultMutableTreeNode)dest.getLastPathComponent();
                TreePath path = tree.getSelectionPath();
                DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode)path.getLastPathComponent();
                if(firstNode.getChildCount() > 0 && target.getLevel() < firstNode.getLevel()) {
                    log("Not sure");
                    return false;
                }
                return true; 
        }
        throw new RuntimeException("Programming error"); //Shouldn't be able to get here.
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
            return new NodesTransferable(copies);
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
        //Implments how to import a transfer (drop operation);
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        List<DefaultMutableTreeNode> nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (List<DefaultMutableTreeNode>)t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
        } catch(java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
        // Get drop location info.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode parent =
            (DefaultMutableTreeNode)dest.getLastPathComponent();
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

    public class NodesTransferable implements Transferable {
        List<DefaultMutableTreeNode> nodes;

        public NodesTransferable(List<DefaultMutableTreeNode> nodes) {
            this.nodes = nodes;
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}