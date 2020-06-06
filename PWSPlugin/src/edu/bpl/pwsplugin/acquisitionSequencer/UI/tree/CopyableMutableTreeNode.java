/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import com.google.gson.Gson;
import edu.bpl.pwsplugin.utils.GsonUtils;
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
        return CopyableMutableTreeNode.fromJson(this.toJson(), this.getClass());
    }
    
    public String toJson() {
        Gson gson = GsonUtils.getGson();
        return gson.toJson(this);
    }
    
    public static CopyableMutableTreeNode fromJson(String s, Class clazz) {
        Gson gson = GsonUtils.getGson();
        return (CopyableMutableTreeNode) gson.fromJson(s, clazz);
    }
    
}