/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import java.awt.datatransfer.DataFlavor;

/**
 *
 * @author nick
 */
public class DataFlavors {
    public static DataFlavor CopiedNodeDataFlavor;

    private final static String CopiedNodeDataFlavorMime = DataFlavor.javaJVMLocalObjectMimeType +  ";class=\"" + CopyableMutableTreeNode.class.getName() + "\"";
    
    static {
        CopiedNodeDataFlavor = new DataFlavor(CopyableMutableTreeNode.class, "CopyableMutableTreeNode");
    }
}
