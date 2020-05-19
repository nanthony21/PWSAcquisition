/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyMoveTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
import java.awt.Dimension;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
class SequenceTree extends TreeDragAndDrop {
    public SequenceTree() {
        super(new CopyMoveTransferHandler());
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sequence Root");
        
        model.setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TreeRenderers.SequenceTreeRenderer());
        
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