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

package edu.bpl.pwsplugin.acquisitionsequencer.UI.tree;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class DataFlavors {

   public static DataFlavor CopiedNodeDataFlavor;

   private final static String CopiedNodeDataFlavorMime =
         DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + CopyableMutableTreeNode.class
               .getName() + "\"";

   static {
      try {
         CopiedNodeDataFlavor = new DataFlavor(CopiedNodeDataFlavorMime, "CopyableMutableTreeNode",
               CopyableMutableTreeNode.class
                     .getClassLoader()); // If we don's specify this classloader we get a classdefnotfounderror when running from a .JAR.
      } catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }
}
