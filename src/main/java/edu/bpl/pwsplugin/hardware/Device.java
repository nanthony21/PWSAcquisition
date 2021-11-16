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
import org.micromanager.internal.utils.ReportingUtils;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public interface Device {

   /**
    * Return true if the settings indicate a device that is supported by this class.
    * @return
    */
   boolean identify();

   /**
    * Return a list of strings for every error detected in the configuration. return empty list if no errors found.
    * @return
    */
   List<String> validate();

   /**
    *  One time initialization of device
    * @throws MMDeviceException
    */
   void initialize() throws MMDeviceException;

   /**
    * Make sure this device is ready for usage, may be run many times.
    * @throws MMDeviceException
    */
   void activate() throws MMDeviceException;

   class AutoFinder<T extends Device, S> {

      private final Class<? extends T>[] subClasses;
      private final Class settingClass;
      private final Function<String, S> sGen;

      /**
       *
       * @param settingsClass
       * @param settingsGenerator
       * @param clazz
       */
      public AutoFinder(Class<?> settingsClass, Function<String, S> settingsGenerator,
            Class<? extends T>... clazz) {
         subClasses = clazz;
         settingClass = settingsClass;
         sGen = settingsGenerator;
      }

      /**
       * This is called from within `getAutomaticInstance`. attempts instantiating subclasses for `devName`.
       * If it isn't recognized then we continue searching. Any other exception gets raised.
       * @param devName
       * @return
       */
      public T getAutoInstance(String devName) {
         S settings = sGen.apply(devName);
         for (Class<? extends T> clz : subClasses) {
            T device;
            try {
               device = (T) clz.getDeclaredConstructor(settingClass).newInstance(settings);
               if (!device.identify()) {
                  continue; // The device didn't detect itself in the configuration.
               }
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException me) {
               throw new RuntimeException(me);
            }
            ReportingUtils.logMessage(
                  String.format("Autofinder found device of type %s for device label %s.",
                        device.getClass().toString(), devName));
            return device; //We only get this far if the object successfully initializes.
         }
         return null; //Nothing was identified.
      }

      /**
       * Detect which device is connected automatically, assumes that only one is connected.
       * @param dType
       * @return
       */
      public T scanAllDevices(DeviceType dType) {
         for (String devLabel : Globals.core().getLoadedDevicesOfType(dType)) {
            T device = getAutoInstance(devLabel);
            if (device != null) {
               return device;
            }
         }
         ReportingUtils.logMessage("Autofinder found no devices.");
         return null; //Nothing was identified.
      }
   }
}
