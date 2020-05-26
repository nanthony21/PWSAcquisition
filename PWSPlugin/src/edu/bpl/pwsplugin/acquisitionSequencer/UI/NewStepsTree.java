/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.ContainerStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.EndpointStepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.StepNode;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
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
        super(new CopyOnlyTransferHandler());
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        Map<Consts.Category, DefaultMutableTreeNode> categories = new HashMap<>();
        for (Consts.Category cat : Consts.Category.values()) {
            String name = Consts.getCategoryName(cat);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
            root.add(node);
            categories.put(cat, node);
        }
        
        for (Consts.Type type : Consts.Type.values()) {
            if (type == Consts.Type.ROOT) { continue; }//ignore this special case
            String name = Consts.getFactory(type).getName();
            JsonableParam settings;
            try {
                settings = Consts.getFactory(type).getSettings().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            StepNode node;
            if (Consts.isContainer(type)) {
                node = new ContainerStepNode(settings, type);
            } else {
                node = new EndpointStepNode(settings, type);
            }
            categories.get(Consts.getFactory(type).getCategory()).add(node);
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
