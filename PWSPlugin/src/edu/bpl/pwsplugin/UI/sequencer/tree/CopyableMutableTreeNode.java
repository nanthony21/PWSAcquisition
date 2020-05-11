/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.tree;

import java.util.UUID;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public class CopyableMutableTreeNode extends DefaultMutableTreeNode {
    //Creates a copy of a tree node and compares as equal to it's parent or another copy with the same parent.
    //Subclasses must 
    UUID id;
    
    public CopyableMutableTreeNode() {
        this(UUID.randomUUID());
    }
    
    private CopyableMutableTreeNode(UUID id) {
        super();
        this.id = id;

    }
    
    public CopyableMutableTreeNode copyWithUUID() {
        CopyableMutableTreeNode n = copy(this, true);
        return n;
    }
    
    public CopyableMutableTreeNode copyWithoutUUID() {
        CopyableMutableTreeNode n = copy(this, false);
        return n;
    }
    
    private CopyableMutableTreeNode copy(CopyableMutableTreeNode node, boolean withUUID) {
        CopyableMutableTreeNode n;
        try {
            n = this.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (withUUID) {
            n.id = node.id;
        }
        this.copyAttributes(node, n);
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            n.add(copy((CopyableMutableTreeNode) node.getChildAt(i), withUUID));
        }
        return n;
    }
    
    protected void copyAttributes(CopyableMutableTreeNode from, CopyableMutableTreeNode to) {
        //Subclasses should override this to copy any additional information.
        to.setUserObject(from.getUserObject());
        to.setAllowsChildren(from.getAllowsChildren());
    }

    public static CopyableMutableTreeNode create(DefaultMutableTreeNode node, Class<? extends CopyableMutableTreeNode> clazz) {
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
    
    @Override
    public boolean equals(Object node) {
        //Determine equality based on UUID.
        if (node instanceof CopyableMutableTreeNode) {
            return this.id.equals(((CopyableMutableTreeNode) node).id);
        } else {
            return false;
        }
    }
    
    //TODO isNodeAncector, isNodeDescendant need to have the UUID comparison behavior implemented.
}