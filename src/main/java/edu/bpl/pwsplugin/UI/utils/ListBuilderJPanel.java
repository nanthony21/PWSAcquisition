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

package edu.bpl.pwsplugin.UI.utils;

import java.awt.LayoutManager;

/**
 * @param <T> The class of the object that hold settings. Must be an iterable.
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class ListBuilderJPanel<T extends Iterable> extends BuilderJPanel<T> {

   //A base class for a UI component representing a list of UIbuildable objects. Right now
   //this is only implemented by ListCardUI. other implementations are possible.
   public ListBuilderJPanel(LayoutManager layout, Class<T> clazz) {
      super(layout, clazz);
   }
}
