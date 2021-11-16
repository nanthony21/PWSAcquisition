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

package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.utils.JsonableParam;
import org.micromanager.PositionList;

/**
 * A collection of settings for the various types of steps.
 *
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class SequencerSettings {
   public static class RootStepSettings extends JsonableParam {
      public String directory = "";
      public String author = "";
      public String project = "";
      public String cellLine = "";
      public String description = "";
   }

   public static class EnterSubfolderSettings extends JsonableParam {
      public String relativePath = "";
   }
}
