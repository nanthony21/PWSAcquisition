package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionsequencer.SequencerConsts.Type;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.ContainerStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.RootStep;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This object can be `waited` on to wait for the status of `running` to change.
 */
public class Sequencer {
   private final SequencerFactoryManager factoryManager = new SequencerFactoryManager();
   private boolean running = false;
   private RootStep rootStep = (RootStep) factoryManager.getFactory(Type.ROOT.name()).createStep();
   private ThrowingFunction<AcquisitionStatus, Void> publishCallback;
   private ThrowingFunction<Void, Void> pauseCallback;

   public SequencerFactoryManager getFactoryManager() {
      return factoryManager;
   }

   public boolean isRunning() {
      return running;
   }

   public void runSequence() {
      AcquisitionStatus startingStatus = new AcquisitionStatus(publishCallback,
            pauseCallback, rootStep);
      SequencerFunction rootFunc = rootStep.getFunction(new ArrayList<>());
      try {
         synchronized (this) {
            running = true;
            this.notifyAll(); // If the sequencer is being waited on then notify that the running status is now updated.
         }
         AcquisitionStatus finalStatus = rootFunc.apply(startingStatus);
      } catch (RuntimeException rte) { // Interrupted exception is caused by the user cancelling. No need to warn the user.
         Throwable exc = rte.getCause();
         if (exc instanceof InterruptedException) { //Interrupted exceptions are caused by the user cancelling, no need to report it.
            Globals.mm().logs().showMessage("User cancelled acquisition.");
         } else if (exc == null) {
            Globals.mm().logs().showError(
                  String.format("Error in sequencer. see core log file. %s", rte.getMessage()));
            Globals.mm().logs().logError(rte);
         } else if (exc instanceof Exception) {
            Globals.mm().logs().showError(
                  String.format("Error in sequencer. See core log file. %s", exc.getMessage()));
            Globals.mm().logs().logError(exc);
         } else {
            Globals.mm().logs()
                  .showError("Acquisition threw a throwable that was not an exception! How?");
         }
      } catch (Throwable th) {
         Globals.mm().logs()
               .showError("Unexpected Throwable thrown from acquisition. Programming error");
         Globals.mm().logs().logError(th);
      } finally {
         synchronized (this) {
            running = false;
            this.notifyAll(); // If the sequencer is being waited on then notify that the running status is now updated.
         }
      }
   }

   public void setCallbacks(ThrowingFunction<AcquisitionStatus, Void> publishCallback,
         ThrowingFunction<Void, Void> pauseCallback) {
      this.publishCallback = publishCallback;
      this.pauseCallback = pauseCallback;
   }

   public List<String> validateSequence() {
      return verifySubSteps(rootStep, new ArrayList<>());
   }

   public void saveSequence(String filePath) throws IOException {
      rootStep.saveToJson(filePath.toString());
   }

   public void loadSequence(String filePath) throws IOException {
      RootStep rootStep;
      try (FileReader reader = new FileReader(filePath.toString())) {
         //Loading from a runtime settings file (the kind automatically saved when an acquisition is run.
         if (filePath.endsWith("rtpwsseq")) {
            rootStep = (RootStep) GsonUtils.getGson()
                  .fromJson(reader, RuntimeSettings.class).getRootStep();
         } else {
            rootStep = GsonUtils.getGson().fromJson(reader, RootStep.class);
         }
      }
      this.rootStep = rootStep;
   }

   public void setRootStep(RootStep step) {
      rootStep = step;
   }

   public RootStep getRootStep() {
      return rootStep;
   }

   private List<String> verifySubSteps(Step parent, List<String> errs) {
      //Recursively validate all steps below `parent` and add the errors to `errs`
      errs.addAll(parent.validate());
      if (parent instanceof ContainerStep) {
         for (Step substep : ((ContainerStep<?>) parent).getSubSteps()) {
            errs.addAll(verifySubSteps(substep, new ArrayList<>()));
         }
      }
      return errs;
   }

}
