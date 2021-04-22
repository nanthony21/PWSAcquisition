///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.UI;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;
import com.cureos.numerics.CobylaExitStatus;
import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.PWSAlbum;
import edu.bpl.pwsplugin.hardware.configurations.HWConfiguration;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.configurations.ImagingConfiguration;
import edu.bpl.pwsplugin.hardware.settings.ImagingConfigurationSettings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;
import org.micromanager.data.Image;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */

class UtilityPanel extends JPanel {

   ExposurePanel expPanel = new ExposurePanel();

   public UtilityPanel() {
      super(new MigLayout());

      expPanel.setBorder(BorderFactory.createEtchedBorder());

      this.add(new JLabel("<html><B>Auto Exposure</B></html>"), "wrap");
      this.add(expPanel);
   }

   public void setActionButtonsEnabled(boolean enable) {
      expPanel.runBtn.setEnabled(enable);
   }
}

class ExposurePanel extends JPanel implements PropertyChangeListener {

   ImprovedComponents.Spinner wv = new ImprovedComponents.Spinner(
         new SpinnerNumberModel(550, 400, 1000, 10));
   ImprovedComponents.Spinner targetIntensity = new ImprovedComponents.Spinner(
         new SpinnerNumberModel(90, 0, 100,
               10)); //Expressed as a percentage of max range of the camera.
   JComboBox<String> config = new JComboBox<>();
   JButton runBtn = new JButton("Run");
   JTextField exposureText = new JTextField();
   PWSAlbum display = new PWSAlbum("Auto Exposure");
   private AutoExposureThread aethread;

   public ExposurePanel() {
      super(new MigLayout());
      Globals.addPropertyChangeListener(this);

      exposureText.setEditable(false);

      this.add(new JLabel("Configuration:"), "gapleft push");
      this.add(config, "wrap, growx");
      this.add(new JLabel("Wavelength:"), "gapleft push");
      this.add(wv, "wrap, growx");
      this.add(new JLabel("Target Intensity (%):"), "gapleft push");
      this.add(targetIntensity, "wrap, growx");
      this.add(runBtn, "wrap, span, align center, growx");
      this.add(new JLabel("Exposure (ms):"), "gapleft push");
      this.add(exposureText, "growx");

      config.addItemListener((evt) -> {
         boolean spectral = Globals.getHardwareConfiguration()
               .getImagingConfigurationByName((String) evt.getItem()).hasTunableFilter();
         wv.setEnabled(spectral); //Disable if we aren't using a spectral camera.
      });

      runBtn.addActionListener((evt) -> {
         ImagingConfiguration conf = Globals.getHardwareConfiguration()
               .getImagingConfigurationByName((String) config.getSelectedItem());
         try {
            this.aethread = new AutoExposureThread(conf, (Integer) wv.getValue(),
                  (Integer) targetIntensity.getValue());
         } catch (MMDeviceException e) {
            Globals.mm().logs().showError(e);
         }
      });
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      //We subscribe to the Globals property changes. This gets fired when a change is detected.
      if (evt.getPropertyName().equals("config")) {
         HWConfiguration cfg = (HWConfiguration) evt.getNewValue();
         List<String> allNames = new ArrayList<>();
         for (ImagingConfigurationSettings setting : cfg.getSettings().configs) {
            allNames.add(setting.name);
         }
         config.setModel(new DefaultComboBoxModel<>(allNames.toArray(new String[allNames.size()])));
      }
   }

   class AutoExposeController {

      Integer wavelength = 500;
      ImagingConfiguration config;

      public AutoExposeController(ImagingConfiguration conf, Integer wavelength)
            throws MMDeviceException {
         config = conf;
         if (config.hasTunableFilter()) {
            config.tunableFilter().setWavelength(wavelength);
         }
      }

      public Double run(Integer targetIntensityPercent) throws MMDeviceException {
         String origCamDevice = Globals.core().getCameraDevice();
         boolean liveModeWasOn = false;
         if (Globals.mm().live().getIsLiveModeOn()) {
            liveModeWasOn = true;
            Globals.mm().live().setLiveMode(false);
         }
         Double initialExposure;
         try {
            this.config
                  .activateConfiguration(); //We need our camera to be "The Camera" for the next part to work.
            initialExposure = config.camera().getExposure();
         } catch (Exception e) {
            throw new MMDeviceException(e);
         }
         Integer maxCounts = ((int) Math.round(Math.pow(2, Globals.core().getImageBitDepth())))
               - 1; //This should be the count when the image is saturated.
         Integer targetCounts = (int) Math.round(maxCounts * (targetIntensityPercent
               / 100.0)); //Calculate target counts from percentage based on camera information.
         Calcfc opt = new Calcfc() { //This class is what we provide to the COBYLA optimizer to execute the optimization.
            @Override
            public double Compute(int n, int m, double[] x, double[] con) {
               //My understanding is that the `con` constraint functions should be negative if they are invalid.
               //con[0] = 95 - x[0]; //Don't go above 95%
               con[1] = x[0] - 5; //Don't go below 5ms

               double newExposure = x[0];
               try {
                  Globals.core().setExposure(AutoExposeController.this.config.camera().getName(),
                        newExposure);
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }
               Image img = Globals.mm().live().snap(false).get(0); //Get an image
               display.addImage(img);
               List<Integer> pix = new ArrayList<>();
               //Unfortunately getting the pixels from the image into a list is not straightforward.
               for (int X = 0; X < img.getWidth(); X++) {
                  for (int Y = 0; Y < img.getHeight(); Y++) {
                     pix.add((int) img.getIntensityAt(X,
                           Y)); //Converting from long to int here is potentially dangerous, but really even high-end cameras are only 16 bit.
                  }
               }
               Integer measured = percentile(pix,
                     99); //The metric that we are optimizing is the intensity (camera counts) of the 99th percentile of camera pixels.
               Integer error = targetCounts - measured; //How far are we from being optimized.
               Globals.mm().logs().logMessage(
                     String.format("AutoExposure: Count error of %d at exposure of %.2f ms", error,
                           newExposure));
               return java.lang.Math
                     .abs(error); //If we don't have abs() here then error can be negative, COBYLA tries to minimize error. so that's not good.
            }
         };

         double[] exposure = {
               initialExposure}; //Initial value of whatever the camera was initially set to.
         double rhoBegin = 20; //The tuning sensitivity at first.
         double rhoEnd = 2; //The tuning sensitivity at the end to finalize.
         CobylaExitStatus status = Cobyla
               .FindMinimum(opt, 1, 2, exposure, rhoBegin, rhoEnd, 0, 100);
         Globals.mm().logs().logMessage(
               String.format("AutoExposure: Finished with status: %s", status.toString()));
         try {
            config.camera().setExposure(exposure[0]); //Apply the optimized exposure value.
            //Globals.core().setCameraDevice(origCamDevice); // Set things back the way they were.
            Globals.mm().app().refreshGUI(); //Show the changed exposure in the GUI.
         } catch (Exception e) {
            throw new MMDeviceException(e);
         }
         if (liveModeWasOn) {
            Globals.mm().live().setLiveMode(true);
         }
         return exposure[0]; //exposure now contains the optimal value.
      }

      private Integer percentile(List<Integer> values, double percentile) {
         Collections.sort(values);
         int index = (int) Math.ceil((percentile / 100) * values.size());
         return values.get(index - 1);
      }
   }

   class AutoExposureThread extends SwingWorker<Void, Void> {

      private final AutoExposeController aec;
      private double exposureResult = 0;
      private final Integer targetIntensityPercent;

      public AutoExposureThread(ImagingConfiguration imConf, Integer wavelength,
            Integer targetIntensity) throws MMDeviceException {
         this.aec = new AutoExposeController(imConf, wavelength);
         this.targetIntensityPercent = targetIntensity;
         ExposurePanel.this.runBtn.setEnabled(false);
         this.execute();
      }

      @Override
      public Void doInBackground() {
         try {
            this.exposureResult = this.aec.run(this.targetIntensityPercent);
         } catch (MMDeviceException ie) {
            Globals.mm().logs().logError(ie);
            Globals.mm().logs().showError(
                  String.format("Error in sequencer. See core log file. %s", ie.getMessage()));
         }
         SwingUtilities.invokeLater(() -> {
            finished();
         });
         return null;
      }

      //public void done() This method has a bug where it can run before the thread actually exits. Use invokelater instead.
      public void finished() {
         ExposurePanel.this.runBtn.setEnabled(true);
         ExposurePanel.this.exposureText.setText(String.format("%.2f", exposureResult));
      }

   }
}