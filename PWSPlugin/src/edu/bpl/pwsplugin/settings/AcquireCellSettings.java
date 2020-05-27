/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.settings;

import edu.bpl.pwsplugin.utils.JsonableParam;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class AcquireCellSettings extends JsonableParam {
    public PWSSettings pwsSettings = new PWSSettings();
    public DynSettings dynSettings = new DynSettings();
    public List<FluorSettings> fluorSettings = new ArrayList<>();

    public AcquireCellSettings() {
        fluorSettings.add(new FluorSettings());
    }
}
