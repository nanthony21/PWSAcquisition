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

package edu.bpl.pwsplugin;

import java.nio.file.Path;

/**
 * @author nick
 */

public class FileSpecs {

   public enum Type { // The types of acquisitions that are supported.
      DYNAMICS,
      PWS,
      FLUORESCENCE
   }

   public static String getFilePrefix(
         Type type) { //Files saved by this acquisition should be renamed to this prefix for easier identification.
      switch (type) {
         case DYNAMICS:
            return "dyn";
         case FLUORESCENCE:
            return "fluor";
         case PWS:
            return "pws";
      }
      throw new RuntimeException(
            "Programming Error in getFilePrefix"); //If we get this far we forgot to handle a case.
   }

   public static String getSubfolderName(
         Type type) { //Files saved by this acquisition should be placed into a subfolder of the "CellX" folder by this name.
      switch (type) {
         case DYNAMICS:
            return "Dynamics";
         case FLUORESCENCE:
            return "Fluorescence";
         case PWS:
            return "PWS";
      }
      throw new RuntimeException(
            "Programming Error in getSubfolderName"); //If we get this far we forgot to handle a case.
   }

   public static Path getCellFolderName(Path dir,
         int cellNum) { //Utility function to get the path to a main "CellX" folder.
      return dir.resolve(String.format("Cell%d", cellNum));
   }
}
