/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author nick
 */
public class Transferables {
    public static class NodesTransferable implements Transferable {
        List<DefaultMutableTreeNode> nodes;
        DataFlavor[] flavors = new DataFlavor[1];

        public NodesTransferable(List<DefaultMutableTreeNode> nodes) {
            this.nodes = nodes;
            try {
                flavors[0] = new DataFlavors.CopiedNodeDataFlavor();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavors[0].equals(flavor);
        }
    }
}
