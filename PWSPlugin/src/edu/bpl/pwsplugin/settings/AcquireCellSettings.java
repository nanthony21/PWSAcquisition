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
    
    public static AcquireCellSettings getDefaultSettings(PWSSystems sys) {
        AcquireCellSettings settings = new AcquireCellSettings();
        PWSSettings pwsSettings = settings.pwsSettings;
        DynSettings dynSettings = settings.dynSettings;
        List<FluorSettings> fluorSettings = settings.fluorSettings; //We'll just leave these as is.
        
        //For now we use the same default settings for all systems for dynamics.
        dynSettings.exposure = 50;
        dynSettings.numFrames = 200;
        dynSettings.wavelength = 550;
        
        //Much of the pws settings are also the same between systems.
        pwsSettings.wvStart = 500;
        pwsSettings.wvStop = 700;
        pwsSettings.wvStep = 2;
        
        switch (sys) {
            case LCPWS1:
                pwsSettings.exposure = 50;
                pwsSettings.externalCamTriggering = false;
                pwsSettings.ttlTriggering = false;
                return settings;
            case LCPWS2:
                pwsSettings.exposure = 100;
                pwsSettings.externalCamTriggering = false;
                pwsSettings.ttlTriggering = true;
                return settings;
            case LCPWS3:
                pwsSettings.exposure = 100;
                pwsSettings.externalCamTriggering = true;
                pwsSettings.ttlTriggering = true;
                return settings;
            case STORM:
                pwsSettings.exposure = 100;
                pwsSettings.externalCamTriggering = false;
                pwsSettings.ttlTriggering = false;
                return settings;
        }
        throw new RuntimeException("Programming error");
    }
}
