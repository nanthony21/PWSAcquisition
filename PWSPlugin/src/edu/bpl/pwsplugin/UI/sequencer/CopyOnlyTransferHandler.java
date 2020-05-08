/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

class CopyOnlyTransferHandler extends TransferHandler {
    DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];

    public CopyOnlyTransferHandler() {
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
        return false;
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
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
            DefaultMutableTreeNode copy = copy(node);
            copies.add(copy);
            for(int i = 1; i < paths.length; i++) {
                DefaultMutableTreeNode next =
                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else { // sibling
                    copies.add(copy(next));
                }
            }
            return new NodesTransferable(copies);
        }
        return null;
    }

    private DefaultMutableTreeNode copy(DefaultMutableTreeNode node) {
        //Copies a node and it's children. doesn't copy parent though.
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.getUserObject());
        newNode.setAllowsChildren(node.getAllowsChildren());
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            newNode.add((MutableTreeNode) copy((DefaultMutableTreeNode) node.getChildAt(i)));
        }
        return newNode;    
        //return new DefaultMutableTreeNode(node);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY; //The type of actions allowed.
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        //Implments how to import a transfer (drop operation);
        return false; //don't allow import.
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