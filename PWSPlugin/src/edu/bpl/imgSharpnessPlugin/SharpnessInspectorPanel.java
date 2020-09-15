/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

/**
 *
 * @author nick
 */
public class SharpnessInspectorPanel extends JPanel {
    
    private final JFreeChart chart = ChartFactory.createXYLineChart(
            null, //title
            "Z", // xlabel
            "Gradient", // ylabel
            new DefaultXYDataset(),
            PlotOrientation.VERTICAL,
            false, // legend
            false, //tooltips
            false //urls
    
    );

    
    private final JLabel sharpnessLabel = new JLabel("Sharpness:");
    private final JLabel zLabel = new JLabel("Z: ");
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
            true// boolean tooltips
    );
    
    private final DefaultXYDataset dset = (DefaultXYDataset) chartPanel.getChart().getXYPlot().getDataset();
    
    
    private final String SERIES_NAME = "DATA";
    
    private ArrayList<Double> xData = new ArrayList<>();
    private ArrayList<Double> yData = new ArrayList<>();

    public SharpnessInspectorPanel() {
        super(new MigLayout("fill"));
        
        resetButton.addActionListener((evt) -> {
            this.clearData();
        });
        
        this.chart.getXYPlot().setDomainCrosshairVisible(true); // An overlay to display the current z position.
        this.chart.getXYPlot().setDomainCrosshairPaint(new Color(0, 0, 0)); // black crosshair
        //make plot transparent
        Color trans = new Color(0xFF, 0xFF, 0xFF, 0);
        chart.setBackgroundPaint(trans);
        chart.getXYPlot().setBackgroundPaint(trans);
        ((NumberAxis) chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);  //Don't always include 0 in the vertical autoranging.
        
        super.add(resetButton, "wrap");
        super.add(chartPanel, "wrap, spanx, grow, pushy");
        super.add(sharpnessLabel);
        super.add(zLabel);
    }
    
    public void setValue(double x, double y) {
        xData.add(x);
        yData.add(y);
        this.updateDataset();
        this.sharpnessLabel.setText(String.format("Sharpness: %.2f", y));
        this.setZPos(x);
    }
    
    public void setZPos(double z) {
        this.zLabel.setText(String.format("Z: %.2f", z));
        this.chart.getXYPlot().setDomainCrosshairValue(z);
    }
    
    public void clearData() {
        xData = new ArrayList<>();
        yData = new ArrayList<>();
        this.updateDataset();
    }
    
    private void updateDataset() {
        double[][] data = new double[2][xData.size()];
        data[0] = xData.stream().mapToDouble(Double::doubleValue).toArray();
        data[1] = yData.stream().mapToDouble(Double::doubleValue).toArray();
        this.dset.addSeries(SERIES_NAME, data);
    }
}
