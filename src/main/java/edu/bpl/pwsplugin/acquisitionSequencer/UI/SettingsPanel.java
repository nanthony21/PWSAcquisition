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

import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.Sequencer;
import edu.bpl.pwsplugin.acquisitionSequencer.UI.tree.TreeDragAndDrop;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
class SettingsPanel extends JPanel implements TreeSelectionListener, FocusListener, MouseListener {
    Map<String, BuilderJPanel> panelTypeMapping = new HashMap<>();
    Step lastSelectedNode = null;
    JPanel cardPanel = new JPanel(new CardLayout());
    JLabel nameLabel = new JLabel();
    JLabel descriptionLabel = new JLabel();
    private final Sequencer sequencer_;
    
    public SettingsPanel(Sequencer sequencer, TreeDragAndDrop... trees) {
        super(new MigLayout());
        sequencer_ = sequencer;
        nameLabel.setFont(new Font("serif", Font.BOLD, 12));
        descriptionLabel.setFont(new Font("serif", Font.ITALIC, 11));
        
        this.add(nameLabel, "wrap");
        this.add(descriptionLabel, "wrap");
        this.add(cardPanel);
        
        //Register as a listener for the trees that we want to display settings for.
        this.addMouseListener(this);
        for (int i=0; i<trees.length; i++) {
            trees[i].tree().addTreeSelectionListener(this);
            trees[i].tree().addFocusListener(this);
        }

        for (SequencerConsts.Type type : SequencerConsts.Type.values()) {
            panelTypeMapping.put(type.name(), sequencer.getFactory(type.name()).createUI());
        }
        
        int maxH = 0;
        int maxW = 0;
        for (Map.Entry<String, BuilderJPanel> e : panelTypeMapping.entrySet()) {
            cardPanel.add(e.getValue(), e.getKey());
            int h = e.getValue().getHeight();
            if (h > maxH) {
                maxH = h;
            }
            int w = e.getValue().getWidth();
            if (w > maxW) {
                maxW = w;
            }
        }

        Dimension dim = new Dimension(maxW, maxH);
        cardPanel.setSize(dim);
        cardPanel.setMinimumSize(dim);
        
        showPanelForType(SequencerConsts.Type.ACQ.name());
    }
    
    public void forceUpdateSettings(SequencerConsts.Type type, JsonableParam settings) {
        //Used to force an update to the settings represeting by the UI outside the operation handled by other methods of this class.
        try {
            panelTypeMapping.get(type).populateFields(settings);
        } catch (BuilderJPanel.BuilderPanelException e) {
            Globals.mm().logs().logError(e);
        }
    }
    
    @Override
    public void valueChanged(TreeSelectionEvent e) { //When a new node is selected in a tree, show the settings for the node.
        Object node = e.getPath().getLastPathComponent();
        if (node instanceof Step) { // Some nodes may be default nodes used as folders. We don't want to respond to those selections.
            saveSettingsOfLastNode();
            updateSettingsFromNewNode((Step) node);
        }
    }
    
    private void saveSettingsOfLastNode() {
        if (this.lastSelectedNode != null) {
            JsonableParam settings;
            try {
                settings = (JsonableParam) panelTypeMapping.get(this.lastSelectedNode.getType()).build();
                this.lastSelectedNode.setSettings(settings);
            } catch (BuilderJPanel.BuilderPanelException e) {
                Globals.mm().logs().logError(e);
                Globals.mm().logs().showError(e);
            }
        } 
    }
    
    private void updateSettingsFromNewNode(Step n) { 
        this.lastSelectedNode = n;
        BuilderJPanel panel = showPanelForType(n.getType());
        try {
            panel.populateFields(n.getSettings());
        } catch (BuilderJPanel.BuilderPanelException exc) {
            Globals.mm().logs().logError(exc);
        }
    }
    
    private BuilderJPanel showPanelForType(String type) {
        nameLabel.setText(sequencer_.getFactory(type).getName());
        descriptionLabel.setText("<html>" + sequencer_.getFactory(type).getDescription() + "</html>"); //The html tags here should enable text wrapping.
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, type.toString());
        return panelTypeMapping.get(type);
    }
    
    @Override
    public void focusGained(FocusEvent evt) { // Clicking from one JTree to another one doesn't fire a TreeSelectionEvent. Force one to happen so the panel always shows the right settings
        saveSettingsOfLastNode();
        TreePath path = ((JTree) evt.getComponent()).getSelectionPath();
        if (path == null) { return; }
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode)path.getLastPathComponent());
        if (node instanceof Step) {
            updateSettingsFromNewNode((Step) node);
        }
    }
    
    @Override
    public void focusLost(FocusEvent evt) {  //When the user clicks on any other component than one of the Trees make sure to save settings.
        saveSettingsOfLastNode();
    } 
    
    @Override
    public void mouseExited(MouseEvent e) { //The focus listeners still sometimes miss an event where we need to save settings.
        Rectangle r = e.getComponent().getBounds(); 
        Point p = e.getPoint(); // This point is relative to the component origin.
        if (p.x < 0 || p.x >= r.width ||  p.y < 0 || p.y >= r.height) { // The mouse exited event fires even if the mouse is actually on a subcomponent of this one. Only fire if the mouse is outside the component.
            saveSettingsOfLastNode();        
        }
    }
    
    //Unused methods required by mouselistener interface
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
}

