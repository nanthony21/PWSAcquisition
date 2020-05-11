/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.tree;

import java.awt.datatransfer.DataFlavor;

/**
 *
 * @author nick
 */
public class DataFlavors {
    public static class CopiedNodeDataFlavor extends DataFlavor {
        private static String mimeType = DataFlavor.javaJVMLocalObjectMimeType +  ";class=\"" + CopyableMutableTreeNode.class.getName() + "\"";
        public CopiedNodeDataFlavor() throws ClassNotFoundException {
            super(mimeType);
        }
    }
}
