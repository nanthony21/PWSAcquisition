/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import java.lang.reflect.InvocationTargetException;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public abstract class CopyableMutableTreeNode extends DefaultMutableTreeNode {
    //Creates a copy of a tree node and compares as equal to it's parent or another copy with the same parent.
    //Subclasses must implement a copy constructor.
    
    /*protected CopyableMutableTreeNode(CopyableMutableTreeNode node) {
        this();
        this.setUserObject(node.getUserObject()); //TODO do we need to deepcopy here? Yes
        this.setAllowsChildren(node.getAllowsChildren());
    }*/
    
    public CopyableMutableTreeNode() {
        super();
    }
    
    public CopyableMutableTreeNode copy() {
        CopyableMutableTreeNode n = create(this);
        return n;
    }
    
    public static CopyableMutableTreeNode create(CopyableMutableTreeNode node) {
        //Subclasses of CopyableMutableTreeNode can be created with this. If a node is not an instance or subclass of CopyableTreeNode it will be converted to CopyableTreeNode
        CopyableMutableTreeNode n;
        try {
            Class<? extends CopyableMutableTreeNode> c;
            if (CopyableMutableTreeNode.class.isAssignableFrom(node.getClass())) {
                c = (Class<? extends CopyableMutableTreeNode>) node.getClass();
            } else {
                c = CopyableMutableTreeNode.class;
            }
            n = c.getDeclaredConstructor(c).newInstance(node);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        for(int iChildren=node.getChildCount(), i=0; i<iChildren; i++) {
            n.add(create((CopyableMutableTreeNode) node.getChildAt(i)));
        }
        return n;
    }
}