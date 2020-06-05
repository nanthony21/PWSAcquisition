/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.metadata;

import mmcorej.org.json.JSONObject;
import java.util.List;
import mmcorej.org.json.JSONArray;
import mmcorej.org.json.JSONException;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class DynamicsMetadata extends MetadataBase{
    private final Double wavelength;
    private final List<Double> times;
    private final Double exposure;
    
    public DynamicsMetadata(MetadataBase base, Double wavelength, List<Double> times, Double exposure) {
        super(base);
        this.wavelength = wavelength;
        this.times = times;
        this.exposure = exposure;
    }
    
    @Override
    public JSONObject toJson() {
        JSONObject md = super.toJson();
        try {
            md.put("wavelength", wavelength);
            md.put("exposure", exposure);
            md.put("times", new JSONArray(times));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return md;
    }
}
