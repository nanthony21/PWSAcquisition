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

import java.text.Format;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.text.DefaultFormatter;

/**
 * @author nicke
 */
public class ImprovedComponents {

   private ImprovedComponents() {
   } //Make private

   public static class Spinner extends JSpinner {
      //By default the JSpinner `getValue` does not update when a valid value is typed in unless focus is then changed to another component.
      //This class makes it so that the value updates as soon as a valid value is typed in. this fixes an annoying bug with many of th settings in the UI
      //where the user would think that they had set a value but actually it had been lost.

      public Spinner() {
         super();
         configureFormatter();
      }

      public Spinner(SpinnerModel model) {
         super(model);
         configureFormatter();

      }

      private void configureFormatter() {
         JFormattedTextField tf = ((JSpinner.DefaultEditor) this.getEditor()).getTextField();
         DefaultFormatter formatter = (DefaultFormatter) tf.getFormatter();
         formatter.setCommitsOnValidEdit(
               true); //This is the key line that make the value update when something is typed in.
      }
   }

   public static class FormattedTextField extends JFormattedTextField {

      //Formatted text field that will update the `value` without having to press enter or change focus.
      public FormattedTextField() {
         super();
         configureFormatter();
      }

      public FormattedTextField(Format format) {
         super(format);
         configureFormatter();
      }

      private void configureFormatter() {
         ((DefaultFormatter) this.getFormatter()).setCommitsOnValidEdit(
               true); //This is the key line that make the value update when something is typed in.
      }
   }
}
