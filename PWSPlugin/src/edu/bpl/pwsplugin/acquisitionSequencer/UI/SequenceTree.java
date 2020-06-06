/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import clojure.lang.Cons;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyMoveTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
class SequenceTree extends TreeDragAndDrop implements KeyListener {
    public SequenceTree() {
        super();
        tree.setTransferHandler(new CopyMoveTransferHandler());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);        

        JsonableParam settings;
        try {
            settings = Consts.getFactory(Consts.Type.ROOT).getSettings().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        ContainerStep root = ((ContainerStep) Consts.getFactory(Consts.Type.ROOT).createStep());
        
        model.setRoot(root);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);
        tree.setCellRenderer(new TreeRenderers.SequenceTreeRenderer());
        
        Dimension d = new Dimension(200, 200);
        setSize(d);
        setMinimumSize(d);
        
        setComponentPopupMenu(new PopupMenu());
        this.addKeyListener(this);
        tree.addKeyListener(this);

    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedNodes();
        }
    }
    
    private void deleteSelectedNodes() {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
            }
        }
    }
    
    class PopupMenu extends JPopupMenu {
        public PopupMenu() {
            super();
            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener((evt)->{
                deleteSelectedNodes();
            });
            
            this.add(deleteItem);
        }
        
        @Override
        public void setVisible(boolean vis) {
            super.setVisible(vis); //just for a debug breakpoint
        }
    }
}