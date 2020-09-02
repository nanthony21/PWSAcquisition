/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import com.google.common.base.Preconditions;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.display.DataViewer;
import org.micromanager.display.inspector.AbstractInspectorPanelController;
import org.micromanager.display.inspector.InspectorPanelListener;
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
    private final JPanel panel_ = new JPanel();
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
          if (viewer_ == null) {
             return;
          }
          setUpChannelHistogramsPanel(
                  viewer_.getDataProvider().getAxisLength(Coords.CHANNEL));
          newDisplaySettings(viewer_.getDisplaySettings());
          updateImageStats(((ImageStatsPublisher) viewer_).getCurrentImagesAndStats());
          String updateRate = studio_.profile().
                  getSettings(IntensityInspectorPanelController.class).
                  getString(HISTOGRAM_UPDATE_FREQUENCY, "1 Hz");
          if (histogramMenuMap_.get(updateRate) != null) {
             handleHistogramUpdateRate(histogramMenuMap_.get(updateRate));
          }
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
       setUpChannelHistogramsPanel(0);
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
}
