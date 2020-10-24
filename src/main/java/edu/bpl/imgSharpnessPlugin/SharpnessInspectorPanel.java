/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
/**
 *
 * @author nick
 */
public class SharpnessInspectorPanel extends JPanel {
    
    private final static String SERIES_NAME = "DATA";

    private final JFreeChart chart = ChartFactory.createXYLineChart(
            null, //title
            "Z", // xlabel
            "Gradient", // ylabel
            new XYSeriesCollection(new XYSeries(SERIES_NAME, true, false)),
            PlotOrientation.VERTICAL,
            false, // legend
            false, //tooltips
            false //urls
    
    );

    private final JFormattedTextField denoiseRadius = new JFormattedTextField(NumberFormat.getIntegerInstance());
    private final JButton resetButton = new JButton("Reset Data");
    
    private final ChartPanel chartPanel = new ChartPanel(
            chart,
            200, // int width,
            200, // int height,
            100, // int minimumDrawWidth,
            100, // int minimumDrawHeight,
            10000, // int maximumDrawWidth,
            10000, // int maximumDrawHeight,
            true, // boolean useBuffer,
            true, // boolean properties,
            true, // boolean copy,
            true, // boolean save,
            true, // boolean print,
            true, // boolean zoom,
            true // boolean tooltips
    );
    
    public final JFreeTextOverlay noRoiOverlay = new JFreeTextOverlay("No Roi Drawn");
    
    private final XYSeriesCollection dset = (XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset();

    public SharpnessInspectorPanel() {
        super(new MigLayout("fill"));
        this.denoiseRadius.setColumns(3);
        
        resetButton.addActionListener((evt) -> {
            this.clearData();
        });
        
        this.chart.getXYPlot().setDomainCrosshairVisible(true); // An overlay to display the current z position.
        this.chart.getXYPlot().setDomainCrosshairPaint(new Color(0, 0, 0)); // black crosshair
        this.chart.getXYPlot().setRangeCrosshairVisible(true); // An overlay to display the current sharpness.
        this.chart.getXYPlot().setRangeCrosshairPaint(new Color(0, 0, 0)); // black crosshair
        this.chartPanel.addOverlay(noRoiOverlay);
        //make plot background transparent
        Color trans = new Color(0xFF, 0xFF, 0xFF, 0);
        chart.setBackgroundPaint(trans);
        chart.getXYPlot().setBackgroundPaint(trans);
        ((NumberAxis) chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);  //Don't always include 0 in the vertical autoranging.
        
        super.add(resetButton);
        super.add(new JLabel("Denoise Blur:"), "gapleft push");
        super.add(denoiseRadius, "wrap");
        super.add(chartPanel, "wrap, spanx, grow, pushy");
    }
    
    public void addDenoiseRadiusValueChangedListener(PropertyChangeListener listener) {
        denoiseRadius.addPropertyChangeListener("value", listener);
    }
    
    public void setDenoiseRadius(int radius) {
        this.denoiseRadius.setValue(radius);
    }
    
    public void setValue(double x, double y) {
        //Add an XY value to the plot. if the x value already exists the old value will be replaced.
        this.dset.getSeries(SERIES_NAME).addOrUpdate(x, y);
        this.chart.getXYPlot().setRangeCrosshairValue(y); //Set the vertical crosshair to the current sharpness value.
        this.setZPos(x);
    }
    
    public void setZPos(double z) {
        //Set the currect z position for the cursor to be set to.
        this.chart.getXYPlot().setDomainCrosshairValue(z);
    }
    
    public void clearData() {
        this.dset.getSeries(SERIES_NAME).clear();
    }
}

