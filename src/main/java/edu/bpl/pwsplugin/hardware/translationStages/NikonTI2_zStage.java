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

package edu.bpl.pwsplugin.hardware.translationStages;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mmcorej.DeviceType;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.micromanager.AutofocusPlugin;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class NikonTI2_zStage extends TranslationStage1d {

   private String offsetDevice; //A stage device with no properties
   private String pfsDevice; //An autofocus device
   private boolean calibrated = false;
   private double[] coef_; //Should be 3 elements giving the quadratic fit of x: um, y: offset. stored in order [intercept, linear, quadratic]
   private final EscapeStatus escStatus = new EscapeStatus();
   private final static double ESCAPE_POSITION = 500.; // Hardcoded z position to move to during escape.

   public NikonTI2_zStage(TranslationStage1dSettings settings) {
      super(settings);
   }

   @Override
   public boolean identify() {
      try {
         return ((Globals.core().getDeviceName(settings.deviceName).equals("ZDrive"))
               &&
               (Globals.core().getDeviceLibrary(settings.deviceName).equals("NikonTi2")));
      } catch (Exception e) {
         return false;
      }
   }

   @Override
   public void initialize() throws MMDeviceException {
      Globals.logger().logDebug("Creating new instance of NikonTI2");
      try {
         String nikonHub = null;
         for (String hubLabel : Globals.core().getLoadedDevicesOfType(DeviceType.HubDevice)) {
            if (Globals.core().getDeviceLibrary(hubLabel).equals("NikonTi2")) {
               nikonHub = hubLabel;
               break;
            }
         }
         if (nikonHub == null) {
            throw new MMDeviceException("No Nikon TI2 Hub device was found.");
         }
         String offsetName = null;
         String statusName = null;
         for (String devLabel : Globals.core().getLoadedPeripheralDevices(nikonHub)) {
            String name;
            name = Globals.core().getDeviceName(devLabel);
            if (name.equals("PFSOffset")) {
               offsetName = devLabel;
            } else if (name.equals("PFS")) {
               statusName = devLabel;
            }
         }
         if (offsetName == null || statusName == null) {
            throw new MMDeviceException("PFS devices were not found.");
         }
         offsetDevice = offsetName;
         pfsDevice = statusName;
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public void activate() throws MMDeviceException {
      // Nothing to do
   }

   private Double getMaximumPFSOffset() {
      return 32500.0;
   }

   private Double getMinimumPFSOffset() {
      return 101.0; //Behavior below this offset is unreliable even though values down to 1 are technically accepted.
   }

   private String getPFSOffsetDeviceName() {
      return offsetDevice;
   }

   private String getZDriveDeviceName() {
      return settings.deviceName;
   }

   private String getPFSDeviceName() {
      return pfsDevice;
   }

   private boolean busy() throws MMDeviceException, InterruptedException {
      try {
         if (getAutoFocusEnabled()) {
            //Setting the PFS offset blocks until the move is complete. Unfortunately the
            //`deviceBusy` method doesn't appear to work for any of the related devices.
            //Z position only updates at 1hz when pfs is enabled, we slowly poll the position to determine when focusing is done
            double origz = Globals.core().getPosition(settings.deviceName);
            try {
               Thread.sleep(1000);
            } catch (InterruptedException ie) {
               Thread.currentThread().interrupt();
               throw ie;
            }
            return !(Math.abs(origz - Globals.core().getPosition(settings.deviceName)) < 0.1);
         } else {
            return Globals.core().deviceBusy(settings.deviceName);
         }
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      if (!identify()) {
         errs.add(String.format("Device %s is not recognized as a Nikon TI2 Z-stage",
               settings.deviceName));
      }
      return errs;
   }

   private void setPFSOffset(double offset) throws MMDeviceException, InterruptedException {
      try {
         Globals.core().setPosition(getPFSOffsetDeviceName(), offset);
         while (busy()) {
            Thread.sleep(10);
         } //block until refocused.
      } catch (InterruptedException | MMDeviceException ee) {
         throw ee;
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   private double getPFSOffset() throws MMDeviceException {
      try {
         return Globals.core().getPosition(getPFSOffsetDeviceName());
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   //TODO there is a major problem with this on the TI2 primarily because the z position only updates at ~1 hz when pfs is on.
   // TODO rather than calibrate over the full PFS range. Limit it to a reasonable um range. That way the polynomial is more accurate for what we actually care about.
   private void calibrate() throws MMDeviceException, InterruptedException {
      //move pfs offset and measure zstage to calibrate pfsConversion.
      List<WeightedObservedPoint> observations = new ArrayList<>();
      double origOffset = getPFSOffset(); //It is vital that we go back to this setting at the end.
      setPFSOffset(getMaximumPFSOffset() / 20); //Move somewhere close to the starting point (0) but not quite.
      try {
         Globals.core().fullFocus(); // Turns on PFS waits to lock and then disables. This essentially just makes sure we lock before proceeding.
         setAutoFocusEnabled(true);
         double zOrig = 0; //This will actually get initialized on the first iteration.
         for (int offset = 0; offset < getMaximumPFSOffset(); offset += (getMaximumPFSOffset() / 4) - 1) {
            setPFSOffset(offset);
            if (offset == 0) {
               //All um measurement are relative to the measurement at pfsOffset = 0
               zOrig = getPosUm();
            }
            double zPosUm = getPosUm() - zOrig;
            double actualOffset = getPFSOffset();
            observations.add(new WeightedObservedPoint(1, zPosUm, actualOffset));
            Globals.logger().logDebug(String.format("Nikon Calibrate: position: %f, offset: %f", zPosUm, actualOffset));
         }
      } catch (InterruptedException | MMDeviceException ie) {
         throw ie;
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
      PolynomialCurveFitter regression = PolynomialCurveFitter.create(2);
      this.coef_ = regression.fit(observations);
      Globals.logger().logDebug("Nikon Calibrate: coefficients: " + Arrays.toString(coef_));

      double offsetStep = (this.getPFSOffset() - origOffset) / 3.0;
      // Going all the way back to the original offset in one step often causes us to lose lock, take it slow.
      for (int i = 3; i >= 0; i--) {
         this.setPFSOffset(origOffset + offsetStep * i);
      }
      this.setPFSOffset(origOffset);
      calibrated = true;
   }

   private double offsetToRelPos(double offset) {
      //Uses the positive quadratic formula to invert the curve fit.
      double numerator = (-coef_[1] + Math
            .sqrt(Math.pow(coef_[1], 2) - 4 * coef_[2] * (coef_[0] - offset)));
      double denominator = (2 * coef_[2]);
      return numerator / denominator;
   }

   private double getOffsetForMicron(double currentOffset, double relUm)
         throws IllegalArgumentException {
      //Use our PFS conversion coefficients to determine what the new PFS offset should be to achieve a relative movement in microns.
      double currentRelMicron = offsetToRelPos(currentOffset);
      double finalRelMicron = currentRelMicron + relUm;
      double newOffset = new PolynomialFunction(coef_).value(finalRelMicron);
      if ((newOffset > getMaximumPFSOffset()) || (newOffset < 0)) {
         throw new IllegalArgumentException(String.format(
               "PFS offset of %f is out of bounds. Relative movement of %f microns is not achievable.",
               newOffset, relUm));
      }
      return newOffset;
   }

   @Override
   public void setPosRelativeUm(double um) throws MMDeviceException, InterruptedException {
      if (um == 0.0) {
         return;
      } // No need to go through calibration and all that if no move is actually requested.
      Globals.logger().logDebug(String.format("Nikon Move Relative Begin: %.2f", um));
      try {
         double initialPos = this.getPosUm();
         if (this.getAutoFocusEnabled()) {
            if (!calibrated) {
               this.calibrate();
            }
            double remainingRelUm =
                  um; //This variable keeps track of how much further we need to go to achieve the original relative movement of `um`
            for (int i = 0; i < 10;
                  i++) { //Due to calibration errors the below code is not accurate enough on one iteration. We give it up to 10 iterations to get within `tolerance` of the correct value.
               double currentOffset = getPFSOffset();
               double currentPos = this.getPosUm();
               double newOffset = this.getOffsetForMicron(currentOffset, remainingRelUm);
               this.setPFSOffset(newOffset); //This will block until the move is finished.
               double newPos = this.getPosUm();
               remainingRelUm -= newPos
                     - currentPos; //subtract the delta-z from this iteration from our remaning distance to go.
               Globals.logger().logDebug(String.format(
                     "Nikon PFS Movement: currentPos %f, newPos %f, remainingRelUm %f, currentOffset %f, newOffset %f",
                     currentPos, newPos, remainingRelUm, currentOffset, newOffset));
               if (Math.abs(remainingRelUm) <= 0.01) {
                  break;
               }//I'm just not sure how to choose the tolerance. However running through 5 iterations without satisfying this requirement is fine.
            }
         } else {
            Globals.core().setRelativePosition(settings.deviceName, um);
            while (this.busy()) {
               Thread.sleep(10);
            } //wait for it to finish focusing.
         }
         double finalPos = this.getPosUm();
         Globals.logger().logDebug(String.format(
               "Nikon PFS Movement: initialPos: %f, finalPos: %f, achievedMovement: %f", initialPos,
               finalPos, finalPos - initialPos));
      } catch (InterruptedException | MMDeviceException ee) {
         throw ee;
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
      Globals.logger().logDebug("Nikon Move Relative End.");
   }

   @Override
   public void setPosUm(double um) throws MMDeviceException, InterruptedException {
      Globals.logger().logDebug(String.format("Nikon Move Absolute Begin: %.2f", um));
      try {
         if (this.getAutoFocusEnabled()) {
            double currentUm = this.getPosUm();
            double relativeUm = um - currentUm;
            this.setPosRelativeUm(relativeUm); // PFS can only be adjusted in a relative context.
         } else {
            Globals.core().setPosition(settings.deviceName, um);
            while (busy()) {
               Thread.sleep(10);
            }
         }
      } catch (InterruptedException | MMDeviceException ee) {
         throw ee;
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
      Globals.logger().logDebug("Nikon Move Absolute End.");
   }

   @Override
   public double getPosUm() throws MMDeviceException {
      try {
         return Globals.core().getPosition(settings.deviceName);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public  boolean supportsEscape() {
      return true;
   }

   @Override
   public void setEscaped(boolean escape) throws MMDeviceException {
      if (!escape) {  // Refocus
         try {
            if (escStatus.escapeAFEnabled) {  // Fancy refocus with PFS
               this.setPosUm(escStatus.escapeRefocusPos - 50);  // Move to a position that should be safe from bumping the sample at the new position but should be close enough to refocus.
               Globals.softwareAutoFocus();
               runFullFocus();
               Thread.sleep(1000); //Without this we will sometimes not actually re-enable pfs for some reason.
               setAutoFocusEnabled(true);
            } else { //Basic refocus
               this.setPosUm(escStatus.escapeRefocusPos);
            }
         } catch (InterruptedException ie) {
            throw new MMDeviceException(ie);
         }
      } else {
         escStatus.escapeAFEnabled = getAutoFocusEnabled();
         if (escStatus.escapeAFEnabled) {
            setAutoFocusEnabled(false);
         }
         escStatus.escapeRefocusPos = getPosUm();

         try {
            this.setPosUm(ESCAPE_POSITION);
         } catch (InterruptedException ie) {
            throw new MMDeviceException(ie);
         }
      }
      escStatus.escaped = escape;  // Set escaped status.
   }

   @Override
   public  boolean isEscaped() {
      return escStatus.escaped;
   }

   @Override
   public boolean hasAutoFocus() {
      return true;
   }

   @Override
   public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
      try {
         Globals.core().enableContinuousFocus(enable); //This returns immediately.
         double stime = System.currentTimeMillis();
         while ((System.currentTimeMillis() - stime)
               < 1000) {//block until it's completed, with a limit of one second.
            if (Globals.core().isContinuousFocusEnabled() == enable) {
               return;
            }
         }
         throw new MMDeviceException("AutoFocus failed to enable to: "
               + enable); //If we got this far then waiting for continuous focus timed out
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean getAutoFocusEnabled() throws MMDeviceException {
      try {
         return Globals.core().isContinuousFocusEnabled();
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean getAutoFocusLocked() throws MMDeviceException {
      //Sometimes right after a move this will report that it is not locked. make sure the stage is not considered busy before checking status.
      try {
         while (busy()) {
            Thread.sleep(10);
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
      try {
         return Globals.core().isContinuousFocusLocked();
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public double runFullFocus() throws MMDeviceException {
      //Move Z in search of a lock for PFS. Note, this is just where PFS locks, it may not mean that the image is focused.
      //Throws MMDevice exception if focus is not found.
      AutofocusPlugin hfe;
      double result;
      try {
         //Rather than simply run the PFS `fullFocus` method we call the "HardwareFocusExtender"
         //plugin which will repeatedly move Z and then try to enable PFS, this can be slow
         //but is much more reliable than any other alternative.
         Globals.mm().getAutofocusManager().setAutofocusMethodByName("HardwareFocusExtender");
         hfe = Globals.mm().getAutofocusManager().getAutofocusMethod();
         hfe.setPropertyValue("HardwareFocusDevice", this.getPFSDeviceName());
         hfe.setPropertyValue("ZDrive", this.getZDriveDeviceName());
         hfe.setPropertyValue("StepSize (um)",
               "5"); //These are the default values of the plugin. are they ok?
         hfe.setPropertyValue("Lower limit (relative, um)", "100");
         hfe.setPropertyValue("Upper limit (relative, um)", "100");
         result = hfe.fullFocus();
      } catch (Exception e) {
         throw new RuntimeException(e); //This shouldn't happen.
      }
      //HFE returns 0 if no focus was found, otherwise it returns the absolute position of the Z stage when focused.
      if (result == 0.0) {
         throw new MMDeviceException("Nikon PFS: No focus lock was found.");
      }
      return result;
   }

   static class EscapeStatus {
      public boolean escaped = false;
      public double escapeRefocusPos = 0;
      public boolean escapeAFEnabled = false;
   }

}

