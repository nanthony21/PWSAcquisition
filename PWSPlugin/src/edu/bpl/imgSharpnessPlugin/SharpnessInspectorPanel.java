/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import java.util.ArrayList;
import java.util.Timer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.DefaultXYDataset;
import org.micromanager.internal.MMStudio;

/**
 *
 * @author nick
 */
public class SharpnessInspectorPanel extends JPanel {
    private final JLabel sharpnessLabel = new JLabel();
    //private final JCheckBox pollCBox = new JCheckBox();
    private final ChartPanel chart = new ChartPanel(ChartFactory.createXYLineChart("Sharpness", "Z", "Gradient", new DefaultXYDataset()));
    private final DefaultXYDataset dset = (DefaultXYDataset) chart.getChart().getXYPlot().getDataset();
    
    
    private final String SERIES_NAME = "DATA";
    
    private ArrayList<Double> xData = new ArrayList<>();
    private ArrayList<Double> yData = new ArrayList<>();

    public SharpnessInspectorPanel() {
        super(new MigLayout());
        
        /*pollCBox.addItemListener((evt) -> {
            if (pollCBox.isSelected()) {
    
            } else {
                
            }
        });*/
        
        super.add(chart);
        super.add(new JLabel("Sharpness:"));
        super.add(sharpnessLabel);
    }
    
    public void setValue(double x, double y) {
        xData.add(x);
        yData.add(y);
        this.updateDataset();
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
