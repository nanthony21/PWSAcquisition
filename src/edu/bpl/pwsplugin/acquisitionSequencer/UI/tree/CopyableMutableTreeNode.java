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
public abstract class CopyableMutableTreeNode extends DefaultMutableTreeNode {
    //Creates a copy of a tree node and compares as equal to it's parent or another copy with the same parent.
    //Subclasses must implement a copy constructor.
    
    public abstract CopyableMutableTreeNode copy();
        //Return a copy of this object that is independent of the original.

}