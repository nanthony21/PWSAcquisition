package edu.bpl.livepws;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorContext;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.data.internal.DefaultImage;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Live PWS
 *
 * Noisy fast acquisition: reduced wavelength, reduced exposure time with smoothing.
 * No binning: just bin in post-process (smoothing)
 *
 * built-in reference (multiple averages of noisy acquisition to determine reference as well as
 * noise correction)
 *
 * live display of RMS for adjusting focus, aperture, comparing regions of dish, etc.
 *
 * Use live plugin concept for this.
 */
@Plugin(type = ProcessorPlugin.class)
public class LivePWS implements ProcessorPlugin, SciJavaPlugin {
   private Studio studio_;

   public LivePWS() {
      int a = 1;
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap settings) {
      return new ProcessorFactory() {
         @Override
         public Processor createProcessor() {
            return new Processor() {
               @Override
               public void processImage(Image image, ProcessorContext context) {
                  //byte[] b = image.getByteArray();
                  configurator.getSettings
                  context.outputImage(image.copyAtCoords(image.getCoords()));
//                  new DefaultImage(b,
//                        image.getWidth(),
//                        image.getHeight(),
//                        image.getBytesPerPixel(),
//                        image.getNumComponents(),
//                        image.getCoords(),
//                        image.getMetadata())
                  //);
               }
            };
         }
      };
   }

   @Override
   public ProcessorConfigurator createConfigurator(PropertyMap settings) {
      return new ProcessorConfigurator() {
         final PropertyMap settings_ = settings;

         @Override
         public void showGUI() {
         }

         @Override
         public void cleanup() {

         }

         @Override
         public PropertyMap getSettings() {
            return settings_;
         }
      };
   }

   /**
    * Receive the Studio object needed to make API calls.
    * @param studio instance of the Micro-Manager Studio object
    */
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   /**
    * Provide a short string identifying the plugin.
    * @return String identifying this plugin
    */
   public String getName() {
      return "PWSLive";
   }

   /**
    * Provide a longer string describing the purpose of the plugin.
    * @return String describing this purpose of this plugin
    */
   public String getHelpText() {
      return "";
   }

   /**
    * Provide a version string.
    * @return Version String
    */
   public String getVersion() { return "--"; }

   /**
    * Provide a copyright string.
    * @return copyright information
    */
   public String getCopyright() { return "Nick Anthony 2021"; }
}
