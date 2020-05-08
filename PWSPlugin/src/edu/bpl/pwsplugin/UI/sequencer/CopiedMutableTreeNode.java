/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public class CopiedMutableTreeNode extends DefaultMutableTreeNode {
    //Creates a copy of a tree node and compares as equal to it's parent or another copy with the same parent.
    DefaultMutableTreeNode originalNode;
    public CopiedMutableTreeNode(DefaultMutableTreeNode node) {
        super(node.getUserObject());
        this.setAllowsChildren(node.getAllowsChildren());
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            this.add(new CopiedMutableTreeNode((DefaultMutableTreeNode) node.getChildAt(i)));
        }    

        //Save a reference to the original
        if (node instanceof CopiedMutableTreeNode) {
            originalNode = ((CopiedMutableTreeNode) node).originalNode;
        } else {
            originalNode = node;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CopiedMutableTreeNode) {
            return this.originalNode == ((CopiedMutableTreeNode) o).originalNode;
        } else {
            return this.originalNode == o;
        }
    }
}