/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import edu.bpl.imgSharpnessPlugin.ui.SharpnessInspectorPanel;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import ij.gui.Roi;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.micromanager.Studio;
import org.micromanager.data.DataProviderHasNewImageEvent;
import org.micromanager.data.Image;
import org.micromanager.data.internal.DefaultImage;
import org.micromanager.display.DataViewer;
import org.micromanager.display.DisplayWindow;
import org.micromanager.display.inspector.AbstractInspectorPanelController;
import org.micromanager.display.inspector.internal.panels.intensity.ImageStatsPublisher;
import org.micromanager.events.StagePositionChangedEvent;
import org.micromanager.internal.utils.MustCallOnEDT;

/**
 *
 * @author N2-LiveCell
 */
public class SharpnessInspectorController extends AbstractInspectorPanelController {
    private static boolean expanded_ = true;  //For some reason a whole new instance of this class is created each time we switch display viewers. Having this variable static allows it's value to stay unchanged between instances.
    private final SharpnessInspectorPanel panel_ = new SharpnessInspectorPanel();
    private DataViewer viewer_;
    private final Studio studio_;
    private boolean autoImageEvaluation_ = true;
    private final SharpnessEvaluator eval_ = new SharpnessEvaluator();
    
    private SharpnessInspectorController(Studio studio) {
        studio_ = studio;
        studio_.events().registerForEvents(this);
                
        panel_.setDenoiseRadius(eval_.denoiseRadius);
        panel_.addPropertyChangeListener("denoiseRadius", (evt) -> {
            eval_.denoiseRadius = ((Long) evt.getNewValue()).intValue();
        });
        
        panel_.addScanRequestedListener((evt) -> {
            SwingWorker worker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    SharpnessInspectorController.this.beginScan(evt.intervalUm(), evt.rangeUm());
                    return null;
                }
            };
            worker.execute();
        });
        
    }
    
    public static SharpnessInspectorController create(Studio studio) {
        return new SharpnessInspectorController(studio);
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
    }

    @Override
    @MustCallOnEDT
    public void detachDataViewer() {
       if (viewer_ == null) {
          return;
       }
       viewer_.getDataProvider().unregisterForEvents(this);
       viewer_.unregisterForEvents(this);
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
    
    @Subscribe
    public void onNewImage(DataProviderHasNewImageEvent evt) {
        ///This is fired because we register for the dataprovider events. Happens each time a new image is available from the provider.
        if (!this.autoImageEvaluation_) {
            return;
        }
        DefaultImage img = (DefaultImage) evt.getImage();
        Roi roi = ((DisplayWindow) viewer_).getImagePlus().getRoi();
        if (roi == null || !roi.isArea()) {
            this.panel_.setRoiSelected(false);
            return;
        }
        this.panel_.setRoiSelected(true);
        Rectangle r = roi.getBounds();
        if (r.width < 5 || r.height < 5) {
            return; //Rectangle must be larger than the kernel used to calculate gradient which is 1x3
        }
        double grad = eval_.evaluateGradient(img, r);
        double z = img.getMetadata().getZPositionUm();
        this.panel_.setValue(z, System.currentTimeMillis(), grad);
    }
    
    @Subscribe
    public void onZPosChanged(StagePositionChangedEvent evt) { //TODO Many z stages don't fire this. use polling instead
        if (!studio_.core().getFocusDevice().equals(evt.getDeviceName())) {
            return; //Stage device names don't match. We only want to use the default focus device.
        }
        this.panel_.setZPos(evt.getPos());
    }
    
    private void beginScan(double intervalUm, double rangeUm) {
        this.panel_.clearData();
        this.panel_.setPlotMode(PlotMode.Z);
        this.autoImageEvaluation_ = false;
        
        if (studio_.live().getIsLiveModeOn()) {
            studio_.live().setLiveMode(false);
        }
        
        Roi roi = ((DisplayWindow) viewer_).getImagePlus().getRoi();
        Rectangle r;
        if (roi == null || !roi.isArea()) {
            r = new Rectangle(  // use full image fov
                    ((DisplayWindow) viewer_).getImagePlus().getWidth(),
                    ((DisplayWindow) viewer_).getImagePlus().getHeight());
        } else {
            r = roi.getBounds();
            //Rectangle must be larger than the kernel used to calculate gradient which is 1x3
            if (r.width < 5) {
                r.setSize(5, r.height);
            } if (r.height < 5) {
                r.setSize(r.width, 5);
            }
        }
        //TODO we should use the PWS Plugin ZStage device not the default one. How is that going to work?
        try {
            long numSteps = Math.round(rangeUm / intervalUm);
            double startingPos = studio_.core().getPosition();
            studio_.core().setRelativePosition(-(rangeUm/2.0)); // Move down by half of the range so that the scan is centered at the starting point.
            while (studio_.core().deviceBusy(studio_.core().getFocusDevice())) { // make sure we moved
                Thread.sleep(50);
            }
            for (int i=0; i<numSteps; i++) {
                studio_.core().setRelativePosition(intervalUm);
                while (studio_.core().deviceBusy(studio_.core().getFocusDevice())) { // make sure we moved
                    Thread.sleep(50);
                }
                
                Image img = studio_.live().snap(true).get(0);
                double sharpness = eval_.evaluateGradient(img, r);
                
                double pos = studio_.core().getPosition();
                panel_.setValue(pos, System.currentTimeMillis(), sharpness);
            }
            studio_.core().setPosition(startingPos);
        } catch (Exception e) {
            studio_.logs().showError(e);
        } finally {
            this.autoImageEvaluation_ = true;
        }
    }

   public static class RequestScanEvent extends ActionEvent {
       private final double interval;
       private final double range;

       public RequestScanEvent(Object source, double intervalUm, double rangeUm) {
           super(source, 0, "startScan");
           interval = intervalUm;
           range = rangeUm;
       }

       public double intervalUm() { return interval; }
       public double rangeUm() { return range; }
   }


   public interface RequestScanListener {
       public void actionPerformed(RequestScanEvent evt);
   }
   
   public static enum PlotMode {
      Time,
      Z;
   }
}