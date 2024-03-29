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

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import edu.bpl.pwsplugin.UI.utils.SingleBuilderJPanel;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps.AcquireTimeSeries;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;


/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class AcquireTimeSeriesFactory implements StepFactory {

   @Override
   public BuilderJPanel<?> createUI() {
      return new TimeSeriesUI();
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return AcquireTimeSeriesFactory.AcquireTimeSeriesSettings.class;
   }

   @Override
   public Step<?> createStep() {
      return new AcquireTimeSeries();
   }

   @Override
   public String getDescription() {
      return "Perform enclosed steps at multiple time points.";
   }

   @Override
   public String getName() {
      return "Time Series";
   }

   @Override
   public String getCategory() {
      return "Sequencing";
   }

   public static class AcquireTimeSeriesSettings extends JsonableParam {

      public int numFrames = 1;
      public double frameIntervalMinutes = 1;

   }
}


class TimeSeriesUI extends SingleBuilderJPanel<AcquireTimeSeriesFactory.AcquireTimeSeriesSettings> {

   ImprovedComponents.Spinner numFrames;
   ImprovedComponents.Spinner frameIntervalMinutes;

   public TimeSeriesUI() {
      super(new MigLayout("insets 5 0 0 0"), AcquireTimeSeriesFactory.AcquireTimeSeriesSettings.class);

      numFrames = new ImprovedComponents.Spinner(new SpinnerNumberModel(1, 1, 1000000000, 1));
      frameIntervalMinutes = new ImprovedComponents.Spinner(
            new SpinnerNumberModel(1.0, 0.0, 1000000000.0, 1.0));
      ((ImprovedComponents.Spinner.DefaultEditor) numFrames.getEditor()).getTextField()
            .setColumns(6);
      ((ImprovedComponents.Spinner.DefaultEditor) frameIntervalMinutes.getEditor()).getTextField()
            .setColumns(6);

      this.add(new JLabel("Number of time frames:"), "gapleft push");
      this.add(numFrames, "wrap");
      this.add(new JLabel("Frame Interval (minutes):"), "gapleft push");
      this.add(frameIntervalMinutes);
   }

   @Override
   public Map<String, Object> getPropertyFieldMap() {
      HashMap<String, Object> m = new HashMap<>();
      m.put("numFrames", numFrames);
      m.put("frameIntervalMinutes", frameIntervalMinutes);
      return m;
   }
}

