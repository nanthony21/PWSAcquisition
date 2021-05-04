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

import edu.bpl.pwsplugin.acquisitionsequencer.factory.BrokenStepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.EnterSubfolderFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.RootStepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;

/**
 * @author nick
 */
public class SequencerConsts {

   public enum Type {  // Built-intypes
      ROOT,
      BROKEN,
      SUBFOLDER
   }

   public static StepFactory getFactory(String type) {
      if (null != type) {
         switch (type) {
            case "ROOT":
               return new RootStepFactory();
            case "BROKEN":
               return new BrokenStepFactory();
            case "SUBFOLDER":
               return new EnterSubfolderFactory();
            default:
               throw new RuntimeException("Unhandled case.");
         }
      }
      throw new RuntimeException("Shouldn't get here.");
   }

   public static void registerGson() {
      for (Type t : Type.values()) {
         getFactory(t.name()).registerGson();
      }
   }
}
