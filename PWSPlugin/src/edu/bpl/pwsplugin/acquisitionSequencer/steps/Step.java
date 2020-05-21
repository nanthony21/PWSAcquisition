/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.acquisitionSequencer.steps;

import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.acquisitionSequencer.settings.SequencerSettings;
import edu.bpl.pwsplugin.utils.JsonableParam;

/**
 *
 * @author nick
 */
public abstract class Step {
    private SequencerSettings settings; 

    public final SequencerSettings getSettings() { return settings; }
    
    public final void setSettings(SequencerSettings settings) { this.settings = settings; }
    
    public abstract SequencerFunction getFunction();
    
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.add("settings", this.getSettings().toJsonObject());
        return obj;
    }
    
    public static Step fromJsonObject(JsonObject obj) {
        JsonObject settingsObj = (JsonObject) obj.get("settings");
        //settingsClass
                JsonableParam.fromJson(sett, clazz)
    }
}
