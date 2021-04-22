package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

/**
 * @author nick
 */
public class Transferables {

   public static class NodesTransferable implements Transferable {

      /* This `transferable` defines how a treenode's data is transmitted during a drag/drop operation.*/
      List<CopyableMutableTreeNode> nodes;
      DataFlavor[] flavors = new DataFlavor[1];

      public NodesTransferable(List<CopyableMutableTreeNode> nodes) {
         this.nodes = nodes;
         flavors[0] = DataFlavors.CopiedNodeDataFlavor;
      }

      @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
         if (!isDataFlavorSupported(flavor)) {
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
