/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin.ui;

import edu.bpl.imgSharpnessPlugin.SharpnessInspectorController;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
    private final JButton resetButton = new JButton("Reset Plot");
    private final JButton scanButton = new JButton("Scan...");
    private final List<SharpnessInspectorController.RequestScanListener> scanRequestedListeners = new ArrayList<>();
    private final ScanDialog scanDlg = new ScanDialog();
    
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
    
    private final JFreeTextOverlay noRoiOverlay = new JFreeTextOverlay("No Roi Drawn");
    
    private final XYSeriesCollection dset = (XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset();

    public SharpnessInspectorPanel() {
        super(new MigLayout("fill"));
        this.denoiseRadius.setColumns(3);
        
        resetButton.addActionListener((evt) -> {
            this.clearData();
        });
        
        scanButton.addActionListener((evt) -> {
            this.scanDlg.setVisible(true);
        });
        
        this.chart.getXYPlot().setDomainCrosshairVisible(true); // An overlay to display the current z position.
        this.chart.getXYPlot().setDomainCrosshairPaint(new Color(0, 0, 0)); // black crosshair
        this.chart.getXYPlot().setRangeCrosshairVisible(true); // An overlay to display the current sharpness.
        this.chart.getXYPlot().setRangeCrosshairPaint(new Color(0, 0, 0)); // black crosshair
        this.chartPanel.addOverlay(noRoiOverlay); // An overlay that tells the user that there needs to be a roi selected.
        this.chartPanel.setPopupMenu(null); // disable the right-click menu
        this.chartPanel.setDomainZoomable(false); // disale zooming by click-drag
        this.chartPanel.setRangeZoomable(false);
        
        //make plot background transparent
        Color trans = new Color(0xFF, 0xFF, 0xFF, 0);
        chart.setBackgroundPaint(trans);
        chart.getXYPlot().setBackgroundPaint(trans);
        ((NumberAxis) chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);  //Don't always include 0 in the vertical autoranging.
        
        super.add(chartPanel, "wrap, spanx, grow, pushy");
        super.add(scanButton);
        super.add(resetButton);
        super.add(new JLabel("Denoise Blur (px):"), "gapleft push");
        super.add(denoiseRadius, "wrap");
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
    
    public void setRoiSelected(boolean hasRoi) {
        //If there is no roi selected use this method to make an overlay visible that tells this to the user.
        if (!hasRoi) {
            if (!this.noRoiOverlay.isVisible()) {
                this.noRoiOverlay.setVisible(true);
                this.repaint(); // Make sure the change in visibility is rendered.
            }
        } else {
            if (this.noRoiOverlay.isVisible()) {
                this.noRoiOverlay.setVisible(false);
                this.repaint(); // Make sure the change in visibility is rendered.
            }
        }
    }
    
    public void addScanRequestedListener(SharpnessInspectorController.RequestScanListener listener) {
        //Add a listener that will be fired when the `scan` button is pressed.
        this.scanRequestedListeners.add(listener);
    }

    private class ScanDialog extends JDialog {
        private final ImprovedComponents.FormattedTextField interval = new ImprovedComponents.FormattedTextField(NumberFormat.getNumberInstance());
        private final ImprovedComponents.FormattedTextField range = new ImprovedComponents.FormattedTextField(NumberFormat.getNumberInstance());
        private final JButton startButton = new JButton("Start");

        public ScanDialog() {
            super(SwingUtilities.getWindowAncestor(SharpnessInspectorPanel.this));
            this.setLayout(new MigLayout());
            this.setLocationRelativeTo(SharpnessInspectorPanel.this);
            this.setTitle("Scan Parameters");

            
            this.interval.setColumns(5);
            this.interval.setValue(0.1);
            this.range.setColumns(5);
            this.range.setValue(5);
            
            this.startButton.addActionListener((evt) -> {
                SharpnessInspectorController.RequestScanEvent event = new SharpnessInspectorController.RequestScanEvent(this, ((Number) interval.getValue()).doubleValue(), ((Number) range.getValue()).doubleValue());
                for (SharpnessInspectorController.RequestScanListener listener : SharpnessInspectorPanel.this.scanRequestedListeners) {
                    listener.actionPerformed(event);
                }
                this.setVisible(false);
            });

            this.add(new JLabel("Interval (um):"));
            this.add(interval, "wrap");
            this.add(new JLabel("Range (um):"));
            this.add(range, "wrap");
            this.add(startButton, "spanx, align center");
            this.pack();
        }
    }
}
