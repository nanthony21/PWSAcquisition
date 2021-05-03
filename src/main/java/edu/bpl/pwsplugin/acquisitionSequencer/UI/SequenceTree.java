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

package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.acquisitionSequencer.Sequencer;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyMoveTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
class SequenceTree extends TreeDragAndDrop implements KeyListener {

   public SequenceTree(Sequencer sequencer) {
      super();
      tree.setTransferHandler(new CopyMoveTransferHandler());
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

      JsonableParam settings;
      try {
         settings = sequencer.getFactory(SequencerConsts.Type.ROOT.name()).getSettings()
               .newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
         throw new RuntimeException(e);
      }

      ContainerStep root = ((ContainerStep) sequencer.getFactory(SequencerConsts.Type.ROOT.name())
            .createStep());

      model.setRoot(root);
      tree.setRootVisible(true);
      tree.setShowsRootHandles(false);
      tree.setCellRenderer(new TreeRenderers.SequenceTreeRenderer());

      Dimension d = new Dimension(200, 200);
      setSize(d);
      setMinimumSize(d);

      setComponentPopupMenu(new PopupMenu());
      this.addKeyListener(this);
      tree.addKeyListener(this);

   }

   public void setRootNodeSelected() {
      tree.setSelectionPath(new TreePath(((ContainerStep) model.getRoot()).getPath()));
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public void keyPressed(KeyEvent e) {
   }

   @Override
   public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_DELETE) {
         deleteSelectedNodes();
      }
   }

   private void deleteSelectedNodes() {
      TreePath[] paths = tree.getSelectionPaths();
      if (paths != null) {
         for (TreePath path : paths) {
            model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
         }
      }
   }

   class PopupMenu extends JPopupMenu {

      public PopupMenu() {
         super();
         JMenuItem deleteItem = new JMenuItem("Delete");
         deleteItem.addActionListener((evt) -> {
            deleteSelectedNodes();
         });

         this.add(deleteItem);
      }

      @Override
      public void setVisible(boolean vis) {
         super.setVisible(vis); //just for a debug breakpoint
      }
   }
}