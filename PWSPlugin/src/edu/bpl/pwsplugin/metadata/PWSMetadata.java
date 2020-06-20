/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.util.List;
import mmcorej.org.json.JSONArray;
import mmcorej.org.json.JSONException;
import mmcorej.org.json.JSONObject;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class PWSMetadata extends MetadataBase{
    private final List<Double> wavelengths;
    private final Double exposure;
    
    public PWSMetadata(MetadataBase base, List<Double> wavelengths, Double exposure) {
        super(base);
        this.wavelengths = wavelengths;
        this.exposure = exposure;
    }
    
    @Override
    public JsonObject toJson() {
        JsonObject md = super.toJson();
        md.add("wavelengths", GsonUtils.getGson().toJsonTree(wavelengths).getAsJsonArray());
        md.addProperty("exposure", exposure);
        return md;
    }
    
}
