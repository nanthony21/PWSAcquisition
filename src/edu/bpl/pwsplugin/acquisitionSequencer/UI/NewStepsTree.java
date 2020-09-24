/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.CopyOnlyTransferHandler;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeRenderers;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.hardware.configurations.HWConfiguration;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.settings.PWSSettingsConsts;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
        
        Map<SequencerConsts.Category, DefaultMutableTreeNode> categories = new HashMap<>();
        for (SequencerConsts.Category cat : SequencerConsts.Category.values()) { //Add folder treenodes for each `Category` we have defined.
            String name = SequencerConsts.getCategoryName(cat);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
            root.add(node);
            categories.put(cat, node);
        }
        
        for (SequencerConsts.Type type : SequencerConsts.Type.values()) { //Add a node for each step type to the appropriate category folder.
            if (type == SequencerConsts.Type.ROOT || type == SequencerConsts.Type.BROKEN) { continue; }//ignore this special case
            JsonableParam settings;
            try {
                settings = SequencerConsts.getFactory(type).getSettings().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            Step node = SequencerConsts.getFactory(type).createStep();
            if (type == SequencerConsts.Type.ACQ) {
                acquisitionStep = node; //Save a reference to the acquisition step.
            }
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
    
    public AcquireCellSettings setDefaultAcquisitionSettings(PWSSettingsConsts.Systems system) {
        //Assign the default settings for the system to the `acquire` setp and return an copy of the new settings.
        acquisitionStep.setSettings(AcquireCellSettings.getDefaultSettings(system));
        return (AcquireCellSettings) acquisitionStep.getSettings();
    }
}
