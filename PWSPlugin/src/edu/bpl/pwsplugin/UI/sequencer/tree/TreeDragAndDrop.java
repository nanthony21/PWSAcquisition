package edu.bpl.pwsplugin.UI.sequencer.tree;

import java.util.Enumeration;
import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.miginfocom.swing.MigLayout;

public class TreeDragAndDrop extends JPanel{
    protected JTree tree = new JTree();
    protected DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    
    public TreeDragAndDrop(TransferHandler handler) {
        super(new MigLayout());
        
        CopyableMutableTreeNode root = CopyableMutableTreeNode.create((DefaultMutableTreeNode) model.getRoot(), CopyableMutableTreeNode.class);
        model.setRoot(root);
        
        //Enumeration<DefaultMutableTreeNode> e = ((DefaultMutableTreeNode) model.getRoot()).breadthFirstEnumeration();
        
        /*while (e.hasMoreElements()) {
            DefaultMutableTreeNode n = e.nextElement();
            CopyableMutableTreeNode n2 = CopyableMutableTreeNode.create(n);
            
        }*/
        
        
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(handler);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        this.add(new JScrollPane(tree));
    }

    public void expandTree(JTree tree) {
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
    
    public JTree tree() {
        return tree;
    }
    
    public DefaultTreeModel model() {
        return model;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel(new MigLayout());
        p.add(new TreeDragAndDrop(new CopyMoveTransferHandler()));
        
        p.add(new TreeDragAndDrop(new CopyOnlyTransferHandler()));
        f.add(p);
        //f.pack();
                
        f.setSize(400,400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
}

    