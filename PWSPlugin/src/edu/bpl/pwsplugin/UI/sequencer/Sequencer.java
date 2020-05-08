/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.sequencer;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nick
 */
public class Sequencer extends JPanel {
    TreeDragAndDrop tdd = new TreeDragAndDrop();
    public Sequencer() {
        super(new MigLayout());
//        this.add(tdd);
    }
    
}
