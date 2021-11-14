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

package edu.bpl.introplugin;

import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.micromanager.IntroPlugin;
import org.micromanager.Studio;
import org.scijava.plugin.Plugin;

/**
 * Simply changes the image shown when the software starts up.
 *
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
@Plugin(type = IntroPlugin.class)
public class PwsIntroPlugin implements IntroPlugin {

   public List<String> getConfigFilePaths() {
      return null;
   }

   public Icon getSplashImage() {
      return new ImageIcon(getClass().getResource("/edu/bpl/icons/splash.png"));
   }

   public String getCopyright() {
      return "Nick Anthony 2020";
   }

   public void setContext(Studio studio) {
   }

   public String getName() {
      return "PWS Intro Plugin";
   }


   public String getHelpText() {
      return "This just changes the Image displayed at startup.";
   }


   public String getVersion() {
      return "0.0.1";
   }

}
