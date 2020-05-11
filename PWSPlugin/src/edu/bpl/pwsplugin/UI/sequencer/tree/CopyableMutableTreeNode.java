/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public class CopyableMutableTreeNode extends DefaultMutableTreeNode {
    //Creates a copy of a tree node and compares as equal to it's parent or another copy with the same parent.
    //Subclasses must 
    
    protected CopyableMutableTreeNode() {
        super();
    }
    
    public CopyableMutableTreeNode copy() {
        CopyableMutableTreeNode n = recursiveCopy(this);
        return n;
    }
    
    private CopyableMutableTreeNode recursiveCopy(CopyableMutableTreeNode node) {
        CopyableMutableTreeNode n;
        try {
            n = this.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        this.copyAttributes(node, n);
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            n.add(recursiveCopy((CopyableMutableTreeNode) node.getChildAt(i)));
        }
        return n;
    }
    
    protected void copyAttributes(CopyableMutableTreeNode from, CopyableMutableTreeNode to) {
        //Subclasses should override this to copy any additional information.
        to.setUserObject(from.getUserObject());
        to.setAllowsChildren(from.getAllowsChildren());
    }

    public static CopyableMutableTreeNode create(DefaultMutableTreeNode node, Class<? extends CopyableMutableTreeNode> clazz) {
        //Converts a defaultMutableTreeNode to a copyable tree node.
        CopyableMutableTreeNode n;
        try {
            n = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        n.setUserObject(node.getUserObject());
        n.setAllowsChildren(node.getAllowsChildren());
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            n.add(create((DefaultMutableTreeNode) node.getChildAt(i), clazz));
        }
        return n;
    }
}