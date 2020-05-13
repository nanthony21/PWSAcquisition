/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.UI.tree;

import java.awt.Color;
import javax.swing.tree.DefaultTreeCellRenderer;
/**
 *
 * @author nick
 */
public class TreeRenderer extends DefaultTreeCellRenderer {
    public TreeRenderer() {
        super();
        setBorderSelectionColor(Color.RED);
    } 
}
