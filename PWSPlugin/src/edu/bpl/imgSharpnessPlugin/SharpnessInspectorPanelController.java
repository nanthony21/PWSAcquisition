/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.imgSharpnessPlugin;

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.derivative.DerivativeType;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.PixelMath;
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayF32;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import ij.gui.Roi;
import java.awt.Rectangle;
import javax.swing.JPanel;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.micromanager.Studio;
import org.micromanager.data.DataProviderHasNewImageEvent;
import org.micromanager.data.internal.DefaultImage;
import org.micromanager.data.internal.DefaultNewImageEvent;
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
public class SharpnessInspectorPanelController extends AbstractInspectorPanelController {
    private boolean expanded_ = true;
    private final SharpnessInspectorPanel panel_ = new SharpnessInspectorPanel();
    private DataViewer viewer_;
    private final Studio studio_;

    private SharpnessInspectorPanelController(Studio studio) {
        studio_ = studio;
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
       studio_.events().registerForEvents(this);
       /*SwingUtilities.invokeLater(() -> {
          setUpChannelHistogramsPanel(
                  viewer_.getDataProvider().getAxisLength(Coords.CHANNEL));
          newDisplaySettings(viewer_.getDisplaySettings());
          updateImageStats(((ImageStatsPublisher) viewer_).getCurrentImagesAndStats());
          String updateRate = studio_.profile().
                  getSettings(IntensityInspectorPanelController.class).
                  getString(HISTOGRAM_UPDATE_FREQUENCY, "1 Hz");
       });*/
    }

    @Override
    @MustCallOnEDT
    public void detachDataViewer() {
        studio_.events().unregisterForEvents(this);
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
    
    @Subscribe
    public void onNewImage(DataProviderHasNewImageEvent evt) {
        ///This is fired because we register for the dataprovider events. Happens each time a new image is available from the provider.
        DefaultImage img = (DefaultImage) evt.getImage();
        Roi roi = ((DisplayWindow) viewer_).getImagePlus().getRoi();
        if (roi == null || !roi.isArea()) {
            return;
        }
        Rectangle r = roi.getBounds();
        if (r.width < 5 || r.height < 5) {
            return; //Rectangle must be larger than the kernel used to calculate gradient which is 1x3
        }
        double grad = evaluateGradient(img, r);
        double z = img.getMetadata().getZPositionUm();
        this.panel_.setValue(z, grad);
    }
    
    @Subscribe
    public void onZPosChanged(StagePositionChangedEvent evt) {
        if (!studio_.core().getFocusDevice().equals(evt.getDeviceName())) {
            return; //Stage device names don't match. We only want to use the default focus device.
        }
        this.panel_.setZPos(evt.getPos());
    }
    
    private double evaluateGradient(DefaultImage img, Rectangle r) {
        GrayF32 im = new GrayF32(r.width, r.height);
        for (int i=0; i<r.width; i++) {
            for (int j=0; j<r.height; j++) {
                long intensity = img.getIntensityAt(r.x + i, r.y + j);
                im.set(i, j, (int) intensity);
            }
        }
        int blurRadius = 3;
        GrayF32 blurred = BlurImageOps.gaussian(im, null, -1, blurRadius, null);
        GrayF32 dx = new GrayF32(im.width, im.height);
        GrayF32 dy = new GrayF32(im.width, im.height);
        GImageDerivativeOps.gradient(DerivativeType.THREE, blurred, dx, dy, BorderType.EXTENDED);
        //Calculate magnitude of gradient
        PixelMath.pow2(dx, dx);
        PixelMath.pow2(dy, dy);
        GrayF32 mag = new GrayF32(dx.width, dx.height);
        PixelMath.add(dx, dy, mag);
        PixelMath.sqrt(mag, mag);
        float[] arr = mag.getData();
        double[] dubArr = new double[arr.length];
        for (int i = 0; i < arr.length; i++)
        {
            dubArr[i] = arr[i];
        }
        return new Percentile().evaluate(dubArr, 95);
    }
}
