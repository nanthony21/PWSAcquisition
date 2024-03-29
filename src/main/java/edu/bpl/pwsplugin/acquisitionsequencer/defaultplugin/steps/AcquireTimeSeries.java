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

package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps;

import edu.bpl.pwsplugin.acquisitionsequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.AcquireTimeSeriesFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.AcquireTimeSeriesFactory.AcquireTimeSeriesSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.IteratingContainerStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import java.util.List;

/**
 * @author Nick Anthony (nickmanthony@hotmail.com)
 */
public class AcquireTimeSeries
      extends IteratingContainerStep<AcquireTimeSeriesSettings> {

   private Integer currentIteration = 0;

   public AcquireTimeSeries() {
      super(new AcquireTimeSeriesFactory.AcquireTimeSeriesSettings(),
            DefaultSequencerPlugin.Type.TIME.name());
   }

   @Override
   public SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      SequencerFunction stepFunction = super.getSubstepsFunction(callbacks);
      AcquireTimeSeriesFactory.AcquireTimeSeriesSettings settings = this.settings;
      return new SequencerFunction() {
         @Override
         public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
            //TIMESERIES execute acquisitionFunHandle repeatedly at a specified time
            //interval. the handle must take as input the Cell number to start at. It
            //will return the number of new acquisitions that it tood.
            double lastAcqTime = 0;
            for (currentIteration = 0; currentIteration < settings.numFrames; currentIteration++) {
               // wait for the specified frame interval before proceeding to next frame
               status.coords().setIterationOfCurrentStep(currentIteration);
               if (currentIteration != 0) { //No pause for the first iteration
                  Integer msgId = status.newStatusMessage("Waiting"); //This will be updated below.
                  int count = 0;
                  while ((System.currentTimeMillis() - lastAcqTime) / 60000
                        < settings.frameIntervalMinutes) {
                     String msg = String.format("Waiting %.1f minutes before acquiring next frame",
                           settings.frameIntervalMinutes
                                 - (System.currentTimeMillis() - lastAcqTime) / 60000);
                     status.updateStatusMessage(msgId, msg);
                     count++;
                     Thread.sleep(500);
                  }
                  if (count == 0) {
                     status.updateStatusMessage(msgId, String.format(
                           "Acquisition took %.1f seconds. Longer than the frame interval.",
                           (System.currentTimeMillis() - lastAcqTime) / 1000));
                  }
               }
               //Save the current time so we can figure out when to start the next acquisition.
               lastAcqTime = System.currentTimeMillis();
               status = stepFunction.apply(status);
               status.newStatusMessage(
                     String.format("Finished time step %d of %d", currentIteration + 1, settings.numFrames));
            }
            currentIteration = 0;
            return status;
         }
      };
   }

   @Override
   protected SimFn getSimulatedFunction() {
      SimFn subStepSimFn = this.getSubStepSimFunction();
      return (Step.SimulatedStatus status) -> {
         int iterations = this.settings.numFrames;
         for (int i = 0; i < iterations; i++) {
            status = subStepSimFn.apply(status);
         }
         return status;
      };
   }

   @Override
   public Integer getCurrentIteration() {
      return currentIteration;
   }

   @Override
   public Integer getTotalIterations() {
      return settings.numFrames;
   }
}
