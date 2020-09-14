/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import com.google.common.base.Preconditions;
import ij.gui.Roi;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.internal.DefaultImage;
import org.micromanager.data.internal.DefaultNewImageEvent;
import org.micromanager.display.DataViewer;
import org.micromanager.display.DisplayWindow;
import org.micromanager.display.inspector.AbstractInspectorPanelController;
import org.micromanager.display.inspector.internal.panels.intensity.ImageStatsPublisher;
import org.micromanager.display.inspector.internal.panels.intensity.IntensityInspectorPanelController;
import static org.micromanager.display.inspector.internal.panels.intensity.IntensityInspectorPanelController.HISTOGRAM_UPDATE_FREQUENCY;
import org.micromanager.internal.utils.MustCallOnEDT;

/**
 *
 * @author N2-LiveCell
 */
public class SharpnessInspectorPanelController extends AbstractInspectorPanelController {
    private boolean expanded_ = true;
    private final SharpnessInspectorPanel panel_ = new SharpnessInspectorPanel();
    private DataViewer viewer_;

    private SharpnessInspectorPanelController(Studio studio) {
        
    }
    
    public static SharpnessInspectorPanelController create(Studio studio) {
        return new SharpnessInspectorPanelController(studio);
    }

    @Override
    public String getTitle() {
       return "Image Sharpness";
    }

    @Override
    public JPanel getPanel() {
       return panel_;
    }

    @Override
    @MustCallOnEDT
    public void attachDataViewer(DataViewer viewer) {
       Preconditions.checkNotNull(viewer);
       if (!(viewer instanceof ImageStatsPublisher)) {
          throw new IllegalArgumentException("Programming error");
       }
       detachDataViewer();
       viewer_ = viewer;
       viewer.registerForEvents(this);
       viewer.getDataProvider().registerForEvents(this);
       SwingUtilities.invokeLater(() -> {
          setUpChannelHistogramsPanel(
                  viewer_.getDataProvider().getAxisLength(Coords.CHANNEL));
          newDisplaySettings(viewer_.getDisplaySettings());
          updateImageStats(((ImageStatsPublisher) viewer_).getCurrentImagesAndStats());
          String updateRate = studio_.profile().
                  getSettings(IntensityInspectorPanelController.class).
                  getString(HISTOGRAM_UPDATE_FREQUENCY, "1 Hz");
       });
    }

    @Override
    @MustCallOnEDT
    public void detachDataViewer() {
       if (viewer_ == null) {
          return;
       }
       viewer_.getDataProvider().unregisterForEvents(this);
       viewer_.unregisterForEvents(this);
       //setUpChannelHistogramsPanel(0);
       viewer_ = null;
    }

    @Override
    public boolean isVerticallyResizableByUser() {
       return true;
    }


    @Override
    public void setExpanded(boolean status) {
       expanded_ = status;
    }

    @Override
    public boolean initiallyExpand() {
       return expanded_;
    }
    
    public void onNewImage(DefaultNewImageEvent evt) {
        ///This is fired when we register for the dataprovider events.
        DefaultImage img = (DefaultImage) evt.getImage();
        Roi roi = ((DisplayWindow) viewer_).getImagePlus().getRoi();
        Rectangle r = roi.getBounds();
        
    }
    
    private double evaluateGradient(DefaultImage img) {
        Roi roi = ((DisplayWindow) viewer_).getImagePlus().getRoi();
        Rectangle r = roi.getBounds();
        img.
    }
}
