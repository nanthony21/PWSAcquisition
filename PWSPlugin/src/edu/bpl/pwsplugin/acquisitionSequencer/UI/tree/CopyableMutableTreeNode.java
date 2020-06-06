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
    //Subclasses must also implement a default constructor
    
    protected CopyableMutableTreeNode() {
        super();
    }
    
    public CopyableMutableTreeNode copy() {
        CopyableMutableTreeNode n = create(this);
        return n;
    }
    
    public static CopyableMutableTreeNode create(DefaultMutableTreeNode node) {
        //Subclasses of CopyableMutableTreeNode can be created with this. If a node is not an instance or subclass of CopyableTreeNode it will be converted to CopyableTreeNode
        CopyableMutableTreeNode n;
        try {
            Class<? extends CopyableMutableTreeNode> c;
            if (CopyableMutableTreeNode.class.isAssignableFrom(node.getClass())) {
                c = (Class<? extends CopyableMutableTreeNode>) node.getClass();
            } else {
                c = CopyableMutableTreeNode.class;
            }
            n = c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        n.copyAttributesFrom(node);
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            n.add(create((DefaultMutableTreeNode) node.getChildAt(i)));
        }
        return n;
    }
    
    protected void copyAttributesFrom(DefaultMutableTreeNode from) {
        CopyableMutableTreeNode.copyDefaultAttributes(from, this);
    }
    
    private static void copyDefaultAttributes(DefaultMutableTreeNode from, DefaultMutableTreeNode to) {
        //Subclasses should override this to copy any additional information.
        to.setUserObject(from.getUserObject()); //TODO do we need to deepcopy here?
        to.setAllowsChildren(from.getAllowsChildren());
    }


}