/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.bpl.pwsplugin.acquisitionsequencer;

import edu.bpl.pwsplugin.acquisitionsequencer.factory.StepFactory;
import java.util.List;

/**
 * @author LCPWS3
 */
public interface SequencerPlugin {

   public StepFactory getFactory(String name);

   public List<String> getAvailableStepNames();
}
