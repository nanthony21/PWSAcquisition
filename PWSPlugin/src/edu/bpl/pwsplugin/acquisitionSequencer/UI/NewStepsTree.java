/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
class NewStepsTree extends TreeDragAndDrop {
    public NewStepsTree() {
        super();
        tree.setTransferHandler(new CopyOnlyTransferHandler());
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        Map<SequencerConsts.Category, DefaultMutableTreeNode> categories = new HashMap<>();
        for (SequencerConsts.Category cat : SequencerConsts.Category.values()) {
            String name = SequencerConsts.getCategoryName(cat);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
            root.add(node);
            categories.put(cat, node);
        }
        
        for (SequencerConsts.Type type : SequencerConsts.Type.values()) {
            if (type == SequencerConsts.Type.ROOT) { continue; }//ignore this special case
            JsonableParam settings;
            try {
                settings = SequencerConsts.getFactory(type).getSettings().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            Step node = SequencerConsts.getFactory(type).createStep();
            node.setSettings(settings);
            categories.get(SequencerConsts.getFactory(type).getCategory()).add(node);
        } 
        
        model.setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TreeRenderers.NewStepsTreeRenderer());
        
        Dimension d = new Dimension(200, 200);
        setSize(d);
        setMinimumSize(d);
    }    
}
