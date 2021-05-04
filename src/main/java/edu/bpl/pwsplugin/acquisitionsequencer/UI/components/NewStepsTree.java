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

package edu.bpl.pwsplugin.acquisitionsequencer.UI.components;

import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFactoryManager;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionsequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class NewStepsTree extends TreeDragAndDrop {

   private final SequencerFactoryManager sequencer_FactoryManager_;

   public NewStepsTree(SequencerFactoryManager sequencerFactoryManager) {
      super();
      sequencer_FactoryManager_ = sequencerFactoryManager;
      tree.setTransferHandler(new CopyOnlyTransferHandler());
      this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      DefaultMutableTreeNode root = new DefaultMutableTreeNode();

      Map<String, DefaultMutableTreeNode> categories = new HashMap<>();
      //Add a node for each step type to the appropriate category folder.
      for (String type : sequencerFactoryManager.getRegisteredFactories()) {
         if (type.equals(SequencerConsts.Type.ROOT.name())
               || type.equals(SequencerConsts.Type.BROKEN.name())) {
            continue; // ignore this special case
         }

         JsonableParam settings;
         StepFactory factory = sequencer_FactoryManager_.getFactory(type);
         try {
            settings = factory.getSettings().newInstance();
         } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
         }
         Step node = factory.createStep();
         node.setSettings(settings);

         String categoryName = factory.getCategory();
         if (categoryName == null) {
            root.add(node);
         } else {
            if (!categories.keySet().contains(
                  categoryName)) { //If the category by this name does not yet exist then create it.
               DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(categoryName);
               root.add(categoryNode);
               categories.put(categoryName, categoryNode);
            }
            categories.get(categoryName).add(node); //Add the new Step node to a category folder
         }
      }

      model.setRoot(root);
      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);
      tree.setCellRenderer(new TreeRenderers.NewStepsTreeRenderer(sequencerFactoryManager));

      Dimension d = new Dimension(200, 200);
      setSize(d);
      setMinimumSize(d);
   }
}
