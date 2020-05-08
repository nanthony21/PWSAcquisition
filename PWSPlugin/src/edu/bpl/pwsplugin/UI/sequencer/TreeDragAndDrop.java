package edu.bpl.pwsplugin.UI.sequencer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.miginfocom.swing.MigLayout;

public class TreeDragAndDrop extends JPanel{
    public TreeDragAndDrop() {
        super(new MigLayout());
        JTree tree = new JTree();
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler());
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        expandTree(tree);
        this.add(new JScrollPane(tree));
    }

    private void expandTree(JTree tree) {
        DefaultMutableTreeNode root =
            (DefaultMutableTreeNode)tree.getModel().getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)e.nextElement();
            if(node.isLeaf()) continue;
            int row = tree.getRowForPath(new TreePath(node.getPath()));
            tree.expandRow(row);
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel(new MigLayout());
        p.add(new TreeDragAndDrop());
        p.add(new TreeDragAndDrop());
        f.add(p);
        f.pack();
                
        f.setSize(400,400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
}

class TreeTransferHandler extends TransferHandler {
    DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    List<DefaultMutableTreeNode> nodesToRemove;

    public TreeTransferHandler() {
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

        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for(int i = 0; i < selRows.length; i++) {
            if(selRows[i] == dropRow) {
                log(String.format("selRows %d %d", dropRow, selRows[i]));
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
                TreePath path = tree.getPathForRow(selRows[0]);
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
            DefaultMutableTreeNode copy = copy(node);
            copies.add(copy);
            toRemove.add(node);
            for(int i = 1; i < paths.length; i++) {
                DefaultMutableTreeNode next =
                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else { // sibling
                    copies.add(copy(next));
                    toRemove.add(next);
                }
            }
            nodesToRemove = toRemove;
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
    