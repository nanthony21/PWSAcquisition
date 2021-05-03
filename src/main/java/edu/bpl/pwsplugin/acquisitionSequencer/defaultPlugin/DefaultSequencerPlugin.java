/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin;

import edu.bpl.pwsplugin.acquisitionsequencer.SequencerPlugin;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.AcquireCellFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.AcquireFromPositionListFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.AcquireTimeSeriesFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.AutoShutterStepFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.ChangeConfigGroupFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.EnterSubfolderFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.EveryNTimesFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.FocusLockFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.PauseFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.SoftwareAutofocusFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.defaultplugin.factories.ZStackFactory;
import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LCPWS3
 */
public class DefaultSequencerPlugin implements SequencerPlugin {

   public enum Type {  // Built-in types
      ACQ,
      POS,
      TIME,
      PFS,
      AF,
      CONFIG,
      PAUSE,
      EVERYN,
      SUBFOLDER,
      ZSTACK,
      AUTOSHUTTER;
   }

   @Override
   public StepFactory getFactory(String type) {
      if (null != type) {
         switch (type) {
            case "ACQ":
               return new AcquireCellFactory();
            case "AF":
               return new SoftwareAutofocusFactory();
            case "PFS":
               return new FocusLockFactory();
            case "POS":
               return new AcquireFromPositionListFactory();
            case "TIME":
               return new AcquireTimeSeriesFactory();
            case "CONFIG":
               return new ChangeConfigGroupFactory();
            case "PAUSE":
               return new PauseFactory();
            case "EVERYN":
               return new EveryNTimesFactory();
            case "SUBFOLDER":
               return new EnterSubfolderFactory();
            case "ZSTACK":
               return new ZStackFactory();
            case "AUTOSHUTTER":
               return new AutoShutterStepFactory();
            default:
               throw new RuntimeException("Shouldn't have happened. Unhandled case.");
         }
      }
      throw new RuntimeException("Shouldn't get here. Unhandled case.");
   }

   @Override
   public List<String> getAvailableStepNames() {
      return Stream.of(Type.values()).map(Type::name).collect(Collectors.toList());
   }
}
