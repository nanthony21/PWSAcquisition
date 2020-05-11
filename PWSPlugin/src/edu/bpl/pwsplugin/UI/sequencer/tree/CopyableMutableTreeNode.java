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
    UUID id;
    
    public CopyableMutableTreeNode() {
        this(UUID.randomUUID());
    }
    
    private CopyableMutableTreeNode(UUID id) {
        super();
        this.id = id;

    }
    
    public CopyableMutableTreeNode copyWithUUID() {
        CopyableMutableTreeNode n = copy(this);
        n.id = this.id;
        return n;
    }
    
    public CopyableMutableTreeNode copyWithoutUUID() {
        CopyableMutableTreeNode n = copy(this);
        n.id = UUID.randomUUID();
        return n;
    }

    private static CopyableMutableTreeNode copy(DefaultMutableTreeNode node) {
        CopyableMutableTreeNode n = new CopyableMutableTreeNode();
        n.setUserObject(node.getUserObject());
        n.setAllowsChildren(node.getAllowsChildren());
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            n.add(copy((DefaultMutableTreeNode) node.getChildAt(i)));
        }
        return n;
    }
    
    @Override
    public boolean equals(Object node) {
        if (node instanceof CopyableMutableTreeNode) {
            return this.id.equals(((CopyableMutableTreeNode) node).id);
        } else {
            return false;
        }
    }
}