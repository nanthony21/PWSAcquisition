package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import java.awt.BorderLayout;
import java.util.Enumeration;
import javax.swing.DropMode;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class TreeDragAndDrop extends JScrollPane {

   protected JTree tree = new JTree();
   protected DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

   public TreeDragAndDrop() {
      super();

      tree.setDragEnabled(true);
      tree.setDropMode(DropMode.ON_OR_INSERT);

      JPanel p = new JPanel(new BorderLayout());
      p.add(tree);

      tree.setInheritsPopupMenu(
            true);//These lines allow us to specify a right click menu further up
      p.setInheritsPopupMenu(true);
      this.setInheritsPopupMenu(true);

      this.setViewportView(p);
   }

   public void expandTree() {
      DefaultMutableTreeNode root =
            (DefaultMutableTreeNode) tree.getModel().getRoot();
      Enumeration e = root.breadthFirstEnumeration();
      while (e.hasMoreElements()) {
         DefaultMutableTreeNode node =
               (DefaultMutableTreeNode) e.nextElement();
         if (node.isLeaf()) {
            continue;
         }
         int row = tree.getRowForPath(new TreePath(node.getPath()));
         tree.expandRow(row);
      }
   }

   public JTree tree() {
      return tree;
   }
}

    