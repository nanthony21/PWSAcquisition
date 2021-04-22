
package edu.bpl.pwsplugin.hardware.tunableFilters;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TunableFilterSettings;
import java.util.List;
import mmcorej.StrVector;

/**
 * @author N2-LiveCell
 */
public abstract class DefaultTunableFilter implements TunableFilter {

   //Provides a default implementation for the abstract methods of `TunableFilter` class.
   //Based on a device name and wavelength property name this class implements methods by calling the corresponding MMCore functions.
   protected final String devName;
   private final TunableFilterSettings _settings;
   private final String wvProp;

   public DefaultTunableFilter(TunableFilterSettings settings, String wvPropertyLabel)
         throws Device.IDException {
      _settings = settings;
      this.devName = settings.name;
      this.wvProp = wvPropertyLabel;
      if (!this.identify()) {
         throw new Device.IDException(
               String.format("Failed to identify class %s for device name %s",
                     this.getClass().toString(), settings.name));
      }
   }

   @Override
   public void setWavelength(int wavelength) throws MMDeviceException {
      try {
         Globals.core().setProperty(devName, wvProp, String.valueOf(wavelength));
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }


   @Override
   public int getWavelength() throws MMDeviceException {
      try {
         int wv = (int) Math.round(Double.valueOf(Globals.core().getProperty(devName, wvProp)));
         return wv;
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean supportsSequencing() {
      return true;
   }

   @Override
   public int getMaxSequenceLength() throws MMDeviceException {
      try {
         return Globals.core().getPropertySequenceMaxLength(devName, wvProp);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public void loadSequence(int[] wavelengthSequence) throws MMDeviceException {
      StrVector strv = new StrVector();
      for (int i = 0; i < wavelengthSequence.length;
            i++) {   //Convert wv from int to string for sending to the device.
         strv.add(String.valueOf(wavelengthSequence[i]));
      }
      try {
         Globals.mm().core().loadPropertySequence(devName, wvProp, strv);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public void startSequence() throws MMDeviceException {
      try {
         Globals.core().startPropertySequence(devName, wvProp);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public void stopSequence() throws MMDeviceException {
      try {
         Globals.core().stopPropertySequence(devName, wvProp);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public boolean isBusy() throws MMDeviceException {
      try {
         return Globals.core().deviceBusy(devName);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public double getDelayMs() throws MMDeviceException {
      try {
         return Globals.core().getDeviceDelayMs(devName);
      } catch (Exception e) {
         throw new MMDeviceException(e);
      }
   }

   @Override
   public TunableFilterSettings getSettings() {
      return _settings;
   }

   @Override
   public void initialize() throws MMDeviceException {
   }  //Not sure what to do here

   @Override
   public void activate() throws MMDeviceException {
   }//Not sure what to do here

   @Override
   public abstract boolean identify();

   @Override
   public abstract List<String> validate();
}
