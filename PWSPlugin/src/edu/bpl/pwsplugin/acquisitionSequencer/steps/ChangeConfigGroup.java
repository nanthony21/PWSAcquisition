/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import edu.bpl.pwsplugin.Globals;
import edu.bpl.pwsplugin.acquisitionSequencer.AcquisitionStatus;
import edu.bpl.pwsplugin.acquisitionSequencer.Consts;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.ChangeConfigGroupSettings;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class ChangeConfigGroup extends ContainerStep {
    public ChangeConfigGroup() {
        super(Consts.Type.CONFIG);
    }

    @Override
    public SequencerFunction getFunction() {
        SequencerFunction subStepFunc = getSubstepsFunction();
        ChangeConfigGroupSettings settings = (ChangeConfigGroupSettings) this.getSettings();
        return new SequencerFunction() {
            @Override
            public AcquisitionStatus applyThrows(AcquisitionStatus status) throws Exception {
                String origConfValue = Globals.core().getCurrentConfig(settings.configGroupName);
                status.update(String.format("Changing %s config group to %s", settings.configGroupName, settings.configValue), status.currentCellNum);
                Globals.core().setConfig(settings.configGroupName, settings.configValue);
                status = subStepFunc.apply(status);
                Globals.core().setConfig(settings.configGroupName, origConfValue);
                status.update(String.format("Changing %s config group back to original setting, %s", settings.configGroupName, origConfValue), status.currentCellNum);
                return status;
            }
        };
    }
    
    
}
