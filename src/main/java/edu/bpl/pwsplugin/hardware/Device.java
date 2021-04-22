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
package edu.bpl.pwsplugin.hardware;

import edu.bpl.pwsplugin.Globals;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;
import mmcorej.DeviceType;

/**
 * @author nicke
 */
public interface Device {

   public boolean identify(); //Return true if the settings indicate a device that is supported by this class.

   public List<String> validate(); //Return a list of strings for every error detected in the configuration. return empty list if no errors found.

   public void initialize() throws MMDeviceException; // One time initialization of device

   public void activate()
         throws MMDeviceException; //Make sure this device is ready for usage, may be run many times.

   public static class IDException extends Exception {

      public IDException(String s) {
         super(s);
      }
   }

   public static class AutoFinder<T extends Device, S> {

      private final Class[] subClasses;
      private final Class sClass;
      private final Function<String, S> sGen;

      public AutoFinder(Class settingsClass, Function<String, S> settingsGenerator,
            Class<? extends T>... clazz) {
         //In order for this to work the `clazz` classes must throw an IDException from the constructor if the device is not recognized.
         subClasses = clazz;
         sClass = settingsClass;
         sGen = settingsGenerator;
      }

      public T getAutoInstance(String devName) {
         //this is called from within `getAutomaticInstance`. attempts instantiating subclasses for `devName`.
         //If it isn't recognized then we get an `IDException` and continue searching. Any other exception gets raised.
         S settings = sGen.apply(devName);
         for (Class clz : subClasses) {
            T device;
            try {
               device = (T) clz.getDeclaredConstructor(sClass).newInstance(settings);
            } catch (InvocationTargetException e) {
               if (e.getCause() instanceof Device.IDException) {
                  continue; //This just means the device wasn't identified. Try the next device
               } else {
                  throw new RuntimeException(e.getCause());
               }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException me) {
               throw new RuntimeException(me);
            }
            Globals.mm().logs().logMessage(
                  String.format("Autofinder found device of type %s for device label %s.",
                        device.getClass().toString(), devName));
            return device; //We only get this far if the object successfully initializes.
         }
         return null; //Nothing was identified.
      }

      public T scanAllDevices(DeviceType dType) {
         //Detect which stage is connected automatically, assumes that only one is connected.
         for (String devLabel : Globals.core().getLoadedDevicesOfType(dType)) {
            T device = getAutoInstance(devLabel);
            if (device != null) {
               return device;
            }
         }
         Globals.mm().logs().logMessage("Autofinder found no devices.");
         return null; //Nothing was identified.
      }
   }
}
