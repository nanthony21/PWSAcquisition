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
package edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.factories;

import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.UI.settings.AcquireCellUI;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.acquisitionSequencer.factory.StepFactory;
import edu.bpl.pwsplugin.acquisitionSequencer.defaultPlugin.steps.AcquireCell;
import edu.bpl.pwsplugin.settings.AcquireCellSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireCellFactory extends StepFactory {

   @Override
   public Class<? extends BuilderJPanel> getUI() {
      return AcquireCellUI.class;
   }

   @Override
   public Class<? extends JsonableParam> getSettings() {
      return AcquireCellSettings.class;
   }

   @Override
   public Class<? extends Step> getStep() {
      return AcquireCell.class;
   }

   @Override
   public String getDescription() {
      return "Acquire PWS, Dynamics, and Fluorescence into a single folder.";
   }

   @Override
   public String getName() {
      return "Acquisition";
   }

   @Override
   public String getCategory() {
      return null;
   }

   @Override
   public SequencerConsts.Type getType() {
      return SequencerConsts.Type.ACQ;
   }
}



