/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.panel.AbstractOverlay;
import org.jfree.chart.panel.Overlay;


/**
 *
 * @author nick
 */
class JFreeTextOverlay extends AbstractOverlay implements Overlay {
    private String _text;
    private boolean _vis = true;
    private final Font _font = new Font("arial", Font.BOLD, 15);
    
    public JFreeTextOverlay(String text) {
        this._text = text;
    }
    
    public void setVisible(boolean visible) {
        this._vis = visible;
    }
    
    public boolean isVisible() {
        return this._vis;
    }
    
    @Override
    public void paintOverlay(Graphics2D g2, ChartPanel chartPanel) {
        if (this._vis) {
            Shape savedClip = g2.getClip();
            Rectangle2D dataArea = chartPanel.getScreenDataArea();
            g2.clip(dataArea);
            g2.setFont(this._font);
            FontMetrics metrics = g2.getFontMetrics();
            int h = metrics.getHeight();
            int w = metrics.stringWidth(_text);
            g2.drawString(this._text, (int) Math.round(dataArea.getCenterX() - (w / 2)), (int) Math.round(dataArea.getCenterY() - (h / 2)));
            
            g2.setClip(savedClip);  
        }
    }
}
