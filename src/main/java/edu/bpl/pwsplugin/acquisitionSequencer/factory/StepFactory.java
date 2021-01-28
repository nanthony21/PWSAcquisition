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
package edu.bpl.pwsplugin.acquisitionSequencer.factory;

import edu.bpl.pwsplugin.acquisitionSequencer.steps.Step;
import edu.bpl.pwsplugin.UI.utils.BuilderJPanel;
import edu.bpl.pwsplugin.acquisitionSequencer.SequencerConsts;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class StepFactory {
    //A factory to provide access to a `Step` class, a JsonableParam settings class, and a UI JPanel to adjust the settings. The `Step` must have a no-args constructor to work with the GSON type adapter.
    public abstract Class<? extends BuilderJPanel> getUI();
    public abstract Class<? extends JsonableParam> getSettings();
    public abstract Class<? extends Step> getStep();
    public abstract String getDescription();
    public abstract String getName();
    public abstract String getCategory();
    public abstract SequencerConsts.Type getType();
    
    public BuilderJPanel createUI() {
        try {    
            return getUI().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Step createStep() {
        try {
            Step step = getStep().newInstance();
            return step;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void registerGson() {
        JsonableParam.registerClass(getSettings());
    }
}
