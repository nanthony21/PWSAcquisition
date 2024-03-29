///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//

package edu.bpl.pwsplugin.acquisitionsequencer.UI.tree;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

public class CopyOnlyTransferHandler extends TransferHandler {

   public CopyOnlyTransferHandler() {
   }

   @Override
   public boolean canImport(TransferHandler.TransferSupport support) {
      return false;
   }

   @Override
   protected Transferable createTransferable(JComponent c) {
      JTree tree = (JTree) c;
      TreePath[] paths = tree.getSelectionPaths();
      if (paths != null) {
         // Make up a node array of copies for transfer and
         // another for/of the nodes that will be removed in
         // exportDone after a successful drop.
         List<CopyableMutableTreeNode> copies = new ArrayList<>();
         if (!(paths[0].getLastPathComponent() instanceof CopyableMutableTreeNode)) {
            return null; //We can't work with non copyable nodes.
         }
         CopyableMutableTreeNode node = (CopyableMutableTreeNode) paths[0].getLastPathComponent();
         copies.add(node.copy());
         for (int i = 1; i < paths.length; i++) {
            CopyableMutableTreeNode next = (CopyableMutableTreeNode) paths[i]
                  .getLastPathComponent();
            // Do not allow higher level nodes to be added to list.
            if (next.getLevel() < node.getLevel()) {
               break;
            } else { // sibling
               copies.add(next.copy());
            }
         }
         return new Transferables.NodesTransferable(copies);
      }
      return null;
   }

   @Override
   public int getSourceActions(JComponent c) {
      return TransferHandler.COPY; //The type of actions allowed.
   }

   @Override
   public boolean importData(TransferHandler.TransferSupport support) {
      //Implments how to import a transfer (drop operation);
      return false; //don't allow import.
   }

   @Override
   public String toString() {
      return getClass().getName();
   }
}