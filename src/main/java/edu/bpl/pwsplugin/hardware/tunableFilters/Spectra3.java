/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LCPWS3
 */
public class Spectra3 implements TunableFilter {

   private final TunableFilterSettings _settings;
   private final static Map<String, Integer> wvMap; // A map of color names used by the device adapter and their peak wavelengths
   private final String devName;

   static {
      wvMap = new HashMap<>();
      wvMap.put("VIOLET", 390);
      wvMap.put("BLUE", 440);
      wvMap.put("CYAN", 475);
      wvMap.put("TEAL", 510);
      wvMap.put("GREEN", 555);
      wvMap.put("YELLOW", 575);
      wvMap.put("RED", 637);
      wvMap.put("NIR", 748);
   }

   public Spectra3(TunableFilterSettings settings) throws Device.IDException {
      this._settings = settings;
      devName = settings.name;
      if (!this.identify()) {
         throw new Device.IDException(
               String.format("Failed to identify class %s for device name %s",
                     this.getClass().toString(), settings.name));
      }
   }

   @Override
   public void setWavelength(int wavelength) throws MMDeviceException {
      int closestWavelength = 0;
      int closeness = 1000000;
      String closestPropName = "";
      Globals.mm().logs().logMessage(String.format("Spectra3 setwavelength %d", wavelength));

      for (Map.Entry<String, Integer> entry : wvMap.entrySet()) {
         int delta = Math.abs(entry.getValue() - wavelength);
         if (delta < closeness) {
            closeness = delta;
            closestWavelength = entry.getValue();
            closestPropName = entry.getKey();
         }
         try {
            Globals.core()
                  .setProperty(this._settings.name, entry.getKey(), 0); // Turn off all wavelengths
         } catch (Exception e) {
            throw new MMDeviceException(e);
         }
      }
      try {
         Globals.mm().logs().logMessage(String.format("Spectra3 enabling %s", closestPropName));
         Globals.core().setProperty(this._settings.name, closestPropName,
               1); // Turn on the closest wavelength
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public int getWavelength() throws MMDeviceException {
      for (Map.Entry<String, Integer> entry : wvMap.entrySet()) {
         String propVal;
         try {
            propVal = Globals.core().getProperty(this._settings.name, entry.getKey());
         } catch (Exception e) {
            throw new MMDeviceException(e);
         }
         if (Integer.valueOf(propVal) == 1) {
            int wv = entry.getValue();
            Globals.mm().logs().logMessage(String.format("Spectra3 getWavelength %d", wv));
            return wv;
         }
      }
      return 637; // No wavelength was on, just say that red was on since we don't have a good handle for this.
   }

   @Override
   public boolean supportsSequencing() {
      return false;
   }

   @Override
   public int getMaxSequenceLength() throws MMDeviceException {
      return 0;
   }

   @Override
   public void loadSequence(int[] wavelengthSequence) throws MMDeviceException {
      throw new MMDeviceException("Sequencing is not supported.");
   }

   @Override
   public void startSequence() throws MMDeviceException {
      throw new MMDeviceException("Sequencing is not supported.");
   }

   @Override
   public void stopSequence() throws MMDeviceException {
      throw new MMDeviceException("Sequencing is not supported.");
   }

   @Override
   public boolean isBusy() throws MMDeviceException {
      try {
         return Globals.core().deviceBusy(this._settings.name);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public double getDelayMs() throws MMDeviceException {
      return 0;
   }

   @Override
   public TunableFilterSettings getSettings() {
      return (TunableFilterSettings) this._settings.copy();
   }

   @Override
   public void initialize() {
   }//Not sure what to do here

   @Override
   public void activate() throws MMDeviceException {
      try {
         for (Map.Entry<String, Integer> entry : wvMap.entrySet()) {
            Globals.core().setProperty(devName, entry.getKey(), 0); // Turn off all
            Globals.core().setProperty(devName, String.format("%s_Intensity", entry.getKey()),
                  1000.0); // Turn intensity all the way up.
         }
         Globals.core().setProperty(devName, "State", 1); // Enable the master enable property.
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean identify() {
      try {
         return Globals.core().getDeviceName(this._settings.name).equals("LightEngine");
      } catch (Exception e) {
         return false;
      }
   }

   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      if (!this.identify()) {
         errs.add("Spectra3: Could not find device named " + this._settings.name + ".");
      }
      return errs;
   }
}
