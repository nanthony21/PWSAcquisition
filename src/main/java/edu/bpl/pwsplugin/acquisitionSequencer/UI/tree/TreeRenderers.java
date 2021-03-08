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
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import edu.bpl.pwsplugin.acquisitionSequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.EndpointStep;
import edu.bpl.pwsplugin.acquisitionSequencer.steps.IteratingContainerStep;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
/**
 * This class contains a collection of renderers that determine how the items of a JTree will look.
 * 
 * @author nick
 */
public class TreeRenderers {
    
    public static class SequenceTreeRenderer extends DefaultTreeCellRenderer {
        //Empty ContainerSteps that are empty are rendered as red text with a special icon.
        //ContainerSteps and Endpoint steps each have a custom icon.
        //A red outline is drawn around active steps.
        public SequenceTreeRenderer() {
            super();
            setBorderSelectionColor(Color.RED); //This helps us to see which tree is active when there are multiple trees in use.
        } 

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
            JLabel comp = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
            if (value instanceof ContainerStep) {
                ContainerStep node = (ContainerStep) value;
                if (node.getChildCount() == 0) { //The container step is empty, set the icon to draw attention to this issue.
                    ImageIcon image = new ImageIcon(getClass().getResource("/edu/bpl/icons/emptyContainerNode.png"));
                    if (image == null) {
                        throw new RuntimeException("Icon failed to load");
                    }
                    comp.setIcon(image);
                    comp.setText("<html>" + comp.getText() + "<font color=red> (empty)</font></html>");
                    return comp;
                } else {
                    ImageIcon image = new ImageIcon(getClass().getResource("/edu/bpl/icons/containerNode.png"));
                    if (image == null) {
                        throw new RuntimeException("Icon failed to load");
                    }
                    comp.setIcon(image);
                    return comp;
                }
            } else if (value instanceof EndpointStep) {
                ImageIcon image = new ImageIcon(getClass().getResource("/edu/bpl/icons/endpointNode.png"));
                if (image == null) {
                    throw new RuntimeException("Icon failed to load");
                }
                comp.setIcon(image);
                return comp;
            } else {
                return comp; //Just return the default rendered componenent.
            }
        }
    }
    
    public static class NewStepsTreeRenderer extends DefaultTreeCellRenderer {
        //This is used for a tree displaying which Step types are available. It is the same as `SequenceTreeRenderer` except that
        //empty ContainerSteps aren't rendered as red or with a special error icon.
        public NewStepsTreeRenderer() {
            super();
            setBorderSelectionColor(Color.RED); //This helps us to see which tree is active when there are multiple trees in use.
        } 
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
            JLabel comp = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
            if (value instanceof ContainerStep) {
                ImageIcon image = new ImageIcon(getClass().getResource("/edu/bpl/icons/containerNode.png"));
                if (image == null) {
                    throw new RuntimeException("Icon failed to load");
                }
                comp.setIcon(image);
                return comp;
            } else if (value instanceof EndpointStep) {
                ImageIcon image = new ImageIcon(getClass().getResource("/edu/bpl/icons/endpointNode.png"));
                if (image == null) {
                    throw new RuntimeException("Icon failed to load");
                }
                comp.setIcon(image);
                return comp;
            } else {
                return comp; //Just return the default rendered componenent.
            }
        }
    }
    
    /**
     * This renderer sets the specifics of how the tree appears when we are showing the current status of running sequence.
     */
    public static class SequenceRunningTreeRenderer extends SequenceTreeRenderer {
        
        public SequenceRunningTreeRenderer() {
            super();
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
            JLabel comp = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
            
            if (value instanceof IteratingContainerStep) { //For a step which acts as a for-loop (IteratingContainerStep) add an indicator of which iteration we are on to the text.
                String initText = comp.getText();
                int currentIteration = ((IteratingContainerStep) value).getCurrentIteration();
                int totalIterations = ((IteratingContainerStep) value).getTotalIterations();
                initText = String.format("%s (%d/%d)", initText, currentIteration, totalIterations);
                comp.setText(initText);
            }
            return comp;
        }
    }
}
