package edu.bpl.pwsplugin.hardware.illumination;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.hardware.Device;
import edu.bpl.pwsplugin.hardware.MMDeviceException;
import edu.bpl.pwsplugin.hardware.settings.IlluminatorSettings;
import java.util.function.Function;


public interface Illuminator extends Device {

   void setShutter(boolean on) throws MMDeviceException;

   static Illuminator getAutomaticInstance(IlluminatorSettings settings) {
      Function<String, IlluminatorSettings> generator = (devName) -> {
         IlluminatorSettings sets = (IlluminatorSettings) settings.copy();
         sets.name = devName;
         return sets;
      };

      Device.AutoFinder<Illuminator, IlluminatorSettings> finder =
            new Device.AutoFinder<>(
                  IlluminatorSettings.class,
                  generator,
                  XCite120LED.class,
                  SimulatedIlluminator.class,
                  DefaultIlluminator.class
            );

      Illuminator illum = finder.getAutoInstance(settings.name);
      if (illum == null) {
         Globals.mm().logs().logMessage("Autofinder No illuminator was found.");
      }
      return illum;
   }

   enum Types {
      XCite120LED,
      Simulated,
   }
}
