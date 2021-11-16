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

package edu.bpl.pwsplugin.acquisitionsequencer.factory;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.EnterSubfolderStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import net.miginfocom.swing.MigLayout;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class EnterSubfolderFactory implements StepFactory {

   @Override
   public BuilderJPanel<?> createUI() {
      return new EnterSubfolderUI();
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return SequencerSettings.EnterSubfolderSettings.class;
   }

   @Override
   public Step<?> createStep() {
      return new EnterSubfolderStep();
   }

   @Override
   public String getDescription() {
      return "Change the folder that enclosed acquisitions are saved to.";
   }

   @Override
   public String getName() {
      return "Enter Subfolder";
   }

   @Override
   public String getCategory() {
      return "Utility";
   }
}


class EnterSubfolderUI extends BuilderJPanel<SequencerSettings.EnterSubfolderSettings> {

   private final JTextField relPath = new JTextField();

   public EnterSubfolderUI() {
      super(new MigLayout("insets 0 0 0 0"), SequencerSettings.EnterSubfolderSettings.class);

      ((AbstractDocument) relPath.getDocument())
            .setDocumentFilter(new MyDocumentFilter()); //Disallow certain characters.

      this.add(new JLabel("Subfolder:"));
      this.add(relPath, "wrap, pushx, growx");
   }

   public SequencerSettings.EnterSubfolderSettings build() {
      String p = this.relPath.getText();
      File f = new File(p);
      SequencerSettings.EnterSubfolderSettings settings =
            new SequencerSettings.EnterSubfolderSettings();
      settings.relativePath = p;
      return settings;
   }

   @Override
   public void populateFields(SequencerSettings.EnterSubfolderSettings settings) {
      this.relPath.setText(settings.relativePath.toString());
   }
}

class MyDocumentFilter extends DocumentFilter {

   private static final String disallowedChars = ".,\\';`";

   @Override
   public void insertString(DocumentFilter.FilterBypass fb, int offset,
         String text, AttributeSet attr) throws BadLocationException {
      StringBuilder buffer = new StringBuilder(text.length());
      for (int i = 0; i < text.length(); i++) {
         char ch = text.charAt(i);
         if (disallowedChars.indexOf(ch) == -1) { //Don't allow disallowed characters.
            buffer.append(ch);
         }
      }
      super.insertString(fb, offset, buffer.toString(), attr);
   }

   @Override
   public void replace(DocumentFilter.FilterBypass fb,
         int offset, int length, String string, AttributeSet attr) throws BadLocationException {
      if (length > 0) {
         fb.remove(offset, length);
      }
      insertString(fb, offset, string, attr);
   }
}