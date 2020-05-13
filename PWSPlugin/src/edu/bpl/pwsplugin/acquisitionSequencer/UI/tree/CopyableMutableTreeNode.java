/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

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
        CopyableMutableTreeNode n = create(this, this.getClass());
        return n;
    }
    
    public static CopyableMutableTreeNode create(DefaultMutableTreeNode node) {
        return create(node, CopyableMutableTreeNode.class);
    }
    
    public static CopyableMutableTreeNode create(DefaultMutableTreeNode node, Class<? extends CopyableMutableTreeNode> clazz) {
        //Subclasses of CopyableMutableTreeNode can be created with this.
        CopyableMutableTreeNode n;
        try {
            n = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        n.copyAttributesFrom(node);
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            n.add(create((DefaultMutableTreeNode) node.getChildAt(i), clazz));
        }
        return n;
    }
    
    protected void copyAttributesFrom(DefaultMutableTreeNode from) {
        CopyableMutableTreeNode.copyDefaultAttributes(from, this);
    }
    
    private static void copyDefaultAttributes(DefaultMutableTreeNode from, DefaultMutableTreeNode to) {
        //Subclasses should override this to copy any additional information.
        to.setUserObject(from.getUserObject());
        to.setAllowsChildren(from.getAllowsChildren());
    }


}