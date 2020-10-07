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
import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.PWSSettingsConsts;
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
    private Step acquisitionStep;
    
    public NewStepsTree() {
        super();
        tree.setTransferHandler(new CopyOnlyTransferHandler());
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        Map<String, DefaultMutableTreeNode> categories = new HashMap<>();
        
        for (SequencerConsts.Type type : SequencerConsts.Type.values()) { //Add a node for each step type to the appropriate category folder.
            if (type == SequencerConsts.Type.ROOT || type == SequencerConsts.Type.BROKEN) { continue; }//ignore this special case
            if (type == SequencerConsts.Type.EVERYN) { continue; } // This step is too complicated and will never be used, just leave it out.
            JsonableParam settings;
            StepFactory factory = SequencerConsts.getFactory(type);
            try {
                settings = factory.getSettings().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            Step node = factory.createStep();
            if (type == SequencerConsts.Type.ACQ) {
                acquisitionStep = node; //Save a reference to the acquisition step.
            }
            node.setSettings(settings);
            
            String categoryName = factory.getCategory();
            if (!categories.keySet().contains(categoryName)) { //If the category by this name does not yet exist then create it.
                DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(categoryName);
                root.add(categoryNode);
                categories.put(categoryName, categoryNode);
            }
            categories.get(categoryName).add(node); //Add the new Step node to a category folder
        } 
        
        model.setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TreeRenderers.NewStepsTreeRenderer());
        
        Dimension d = new Dimension(200, 200);
        setSize(d);
        setMinimumSize(d);
    }
    
    public AcquireCellSettings setDefaultAcquisitionSettings(PWSSettingsConsts.Systems system) {
        //Assign the default settings for the system to the `acquire` setp and return an copy of the new settings.
        acquisitionStep.setSettings(AcquireCellSettings.getDefaultSettings(system));
        return (AcquireCellSettings) acquisitionStep.getSettings();
    }
}
