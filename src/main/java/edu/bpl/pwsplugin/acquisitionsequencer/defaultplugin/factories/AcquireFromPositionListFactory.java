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

package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps.AcquireFromPositionList;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.awt.Container;
import java.awt.Dimension;
import net.miginfocom.swing.MigLayout;
import org.micromanager.PositionList;
import org.micromanager.internal.positionlist.PositionListDlg;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireFromPositionListFactory extends StepFactory {

   @Override
   public BuilderJPanel<?> createUI() {
      return new AcquirePostionsUI();
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return SequencerSettings.AcquirePositionsSettings.class;
   }

   @Override
   public Step<?> createStep() {
      return new AcquireFromPositionList();
   }

   @Override
   public String getDescription() {
      return "Perform enclosed steps at each position in the list. Special commands can be enclosed in "
            + "\"-\". Position names containing:\n\"-ZPFS-\": Disable PFS for this position then reenable."
            + "\n\"-APFS-\": Software autofocus followed by enabling PFS. "
            + "\n\"-PFS-\": Enable PFS and then disable. "
            + "\n\"-ESC-\": Escape the stage before moving to "
            + "position and then refocus.";
   }

   @Override
   public String getName() {
      return "Multiple Positions";
   }

   @Override
   public String getCategory() {
      return "Sequencing";
   }
}

class AcquirePostionsUI extends BuilderJPanel<SequencerSettings.AcquirePositionsSettings> {

   PositionListDlg dlg;

   public AcquirePostionsUI() {
      super(new MigLayout("insets 0 0 0 0, fill"),
            SequencerSettings.AcquirePositionsSettings.class);
      dlg = new PositionListDlg(Globals.mm(), new PositionList());
      Container pane = dlg
            .getContentPane(); //We create a dialog, then steal in contents and put them in our own window, kind of hacky.
      pane.setPreferredSize(new Dimension(100, pane.getHeight())); //Make it a bit slimmer.
      this.add(pane, "grow");
   }

   @Override
   public SequencerSettings.AcquirePositionsSettings build() {
      SequencerSettings.AcquirePositionsSettings settings =
            new SequencerSettings.AcquirePositionsSettings();
      settings.posList = dlg.getPositionList();
      return settings;
   }

   @Override
   public void populateFields(SequencerSettings.AcquirePositionsSettings settings) {
      dlg.setPositionList(settings.posList);
   }
}
