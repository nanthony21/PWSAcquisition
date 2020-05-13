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

/**
 *
 * @author nick
 */
public class Transferables {
    public static class NodesTransferable implements Transferable {
        List<CopyableMutableTreeNode> nodes;
        DataFlavor[] flavors = new DataFlavor[1];

        public NodesTransferable(List<CopyableMutableTreeNode> nodes) {
            this.nodes = nodes;
            flavors[0] = DataFlavors.CopiedNodeDataFlavor;
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