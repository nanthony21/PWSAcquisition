package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories;

import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents.Spinner;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.WaitStepFactory.WaitStepSettings;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.steps.WaitStep;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.JsonableParam;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

public class WaitStepFactory implements StepFactory {

   @Override
   public BuilderJPanel<?> createUI() {
      return new WaitStepUI();
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return WaitStepSettings.class;
   }

   @Override
   public Step<?> createStep() {
      return new WaitStep();
   }

   @Override
   public String getDescription() {
      return "Simply waits for a given number of seconds.";
   }

   @Override
   public String getName() {
      return "Wait";
   }

   @Override
   public String getCategory() {
      return "Utility";
   }

   public static class WaitStepSettings extends JsonableParam {
      public double waitTime = 1.;
   }

}

class WaitStepUI extends BuilderJPanel<WaitStepSettings> {
   private final ImprovedComponents.Spinner spinner = new Spinner(new SpinnerNumberModel(1., 0., 1e6, 1.));

   public WaitStepUI() {
      super(new MigLayout(), WaitStepSettings.class);
      add(new JLabel("Wait time (s):"));
      add(spinner);
   }


   @Override
   public WaitStepSettings build() throws BuilderPanelException {
      WaitStepSettings settings = new WaitStepSettings();
      settings.waitTime = (double) spinner.getValue();
      return settings;
   }

   @Override
   public void populateFields(WaitStepSettings settings) throws BuilderPanelException {
      spinner.setValue(settings.waitTime);
   }
}

