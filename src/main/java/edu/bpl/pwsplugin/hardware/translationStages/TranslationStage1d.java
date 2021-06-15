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

import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.TranslationStage1dSettings;
import java.beans.PropertyChangeListener;
import java.util.function.Function;
import mmcorej.DeviceType;


/**
 * @author nicke
 */
public abstract class TranslationStage1d implements Device {

   protected final TranslationStage1dSettings settings;

   @Override
   public void activate() {
   } //Nothing to do here

   @Override
   public void initialize() {
   } //Nothing to do here

   public TranslationStage1d(TranslationStage1dSettings settings) throws IDException {
      this.settings = settings;
      if (!this.identify()) {
         throw new Device.IDException(
               String.format("Failed to identify class %s for device name %s",
                     this.getClass().toString(), settings.deviceName));
      }
   }

   public abstract double getPosUm() throws MMDeviceException;

   public abstract void setPosUm(double um) throws MMDeviceException, InterruptedException;

   public abstract void setPosRelativeUm(double um) throws MMDeviceException, InterruptedException;

   public abstract boolean supportsEscape();
   public abstract void setEscaped(boolean escape) throws MMDeviceException;
   public abstract boolean isEscaped();

   public abstract boolean hasAutoFocus();

   //The following only need to be implemented if `hasAutoFocus` is true
   public void setAutoFocusEnabled(boolean enable) throws MMDeviceException {
      throw new UnsupportedOperationException();
   }

   public boolean getAutoFocusEnabled() throws MMDeviceException {
      throw new UnsupportedOperationException();
   }

   public boolean getAutoFocusLocked() throws MMDeviceException {
      throw new UnsupportedOperationException();
   }

   public double runFullFocus() throws MMDeviceException {
      //Search for a zStage position where the continuous focus can be locked.
      //Returns the position (microns) where lock is achievable. Throws an exception
      //if no lock is possible.
      throw new UnsupportedOperationException();
   }

   public void addFocusLockListener(PropertyChangeListener listener) {
      throw new UnsupportedOperationException();
   }

   public enum Types {
      NikonTI,
      Simulated,
      NikonTI2,
      PriorProscan3
   }
      
    /*private static final List<Class<? extends TranslationStage1d>> subClasses = 
            Arrays.asList(
                    NikonTI2_zStage.class,
                    NikonTI_zStage.class,
                    SimulationStage1d.class
            );*/
    
    /*public static TranslationStage1d getInstance(TranslationStage1dSettings settings) {
        if (null == settings.stageType) {
            throw new RuntimeException("This shouldn't ever happen.");
        } else switch (settings.stageType) {
            case NikonTI:
                try {
                    return new NikonTI_zStage(settings);
                } catch (Exception e) {
                    Globals.mm().logs().logError(e);
                    return null;
                }
            case NikonTI2:
                try {
                    return new NikonTI2_zStage(settings);
                } catch (Exception e) {
                    Globals.mm().logs().logError(e);
                    return null;
                } 
            case Simulated:
                try {
                    return new SimulationStage1d(settings);
                } catch (IDException e) {
                    Globals.mm().logs().logError(e);
                    return null;
                } 
            default:
                return null; //This shouldn't ever happen.
        }
    }*/
    
    /*private static TranslationStage1d getAutoInstance(String devName) {
        //this is called from within `getAutomaticInstance`. attempts instantiating subclasses for `devName`.
        //If it isn't recognized then we get an `IDException` and continue searching. Any other exception gets raised.
        TranslationStage1dSettings settings = new TranslationStage1dSettings();
        settings.deviceName = devName;
        for (Class clz : subClasses) {
            TranslationStage1d stage;
            try {
                stage = (TranslationStage1d) clz.getDeclaredConstructor(TranslationStage1dSettings.class).newInstance(settings);
            } catch (InvocationTargetException e) { 
                if (e.getCause() instanceof IDException) {
                    continue; //This just means the device wasn't identified. Try the next device
                } else {
                    throw new RuntimeException(e.getCause());
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException me) {
                throw new RuntimeException(me);
            }
            return stage; //We only get this far if the object successfully initializes.
        }
        return null; //Nothing was identified.
    }
    
    public static TranslationStage1d getAutomaticInstance() {
        //Detect which stage is connected automatically, assumes that only one is connected.
        for (String devLabel : Globals.core().getLoadedDevicesOfType(DeviceType.StageDevice)) {
            TranslationStage1d stage = getAutoInstance(devLabel);
            if (stage != null) {
                return stage;
            }
        }
        return null; //Nothing was identified.
    }*/

   public static TranslationStage1d getAutomaticInstance() {

      Function<String, TranslationStage1dSettings> generator = (devName) -> {
         TranslationStage1dSettings settings = new TranslationStage1dSettings();
         settings.deviceName = devName;
         return settings;
      };

      Device.AutoFinder<TranslationStage1d, TranslationStage1dSettings> finder =
            new Device.AutoFinder<>(
                  TranslationStage1dSettings.class,
                  generator,
                  NikonTI2_zStage.class,
                  NikonTI_zStage.class,
                  SimulationStage1d.class,
                  PriorProscan3.class
            );

      return finder.scanAllDevices(DeviceType.StageDevice);
   }
}

