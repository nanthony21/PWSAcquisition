/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.metadata;

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
    public JSONObject toJson() {
        JSONObject md = super.toJson();
        try {
            md.put("wavelengths", new JSONArray(wavelengths));
            md.put("exposure", exposure);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return md;
    }
    
}
