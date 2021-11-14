package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps;

import edu.bpl.pwsplugin.acquisitionsequencer.SequencerFunction;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.DefaultSequencerPlugin.Type;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.WaitStepFactory.WaitStepSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.EndpointStep;
import java.util.ArrayList;
import java.util.List;

public class WaitStep extends EndpointStep<WaitStepSettings> {
   private boolean running_ = false;

   public WaitStep() {
      super(new WaitStepSettings(), Type.WAIT.name());
   }

   @Override
   protected SequencerFunction getStepFunction(List<SequencerFunction> callbacks) {
      WaitStepSettings settings = getSettings();
      return (status) -> {
         running_ = true;
         status.newStatusMessage(String.format("Waiting for %.1f seconds", settings.waitTime));
         Thread.sleep((int) (settings.waitTime * 1000));
         running_ = false;
         return status;
      };
   }

   @Override
   protected SimFn getSimulatedFunction() {
      return (status) -> status; // Do nothing.
   }

   @Override
   public List<String> validate() {
      List<String> errs = new ArrayList<>();
      if (getSettings().waitTime < 0) {
         errs.add(String.format("A wait time of %f seconds is not permitted", getSettings().waitTime));
      }
      return errs;
   }

   @Override
   public boolean isRunning() {
      return running_;
   }
}
