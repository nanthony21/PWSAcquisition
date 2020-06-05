/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.metadata;

import mmcorej.org.json.JSONException;
import mmcorej.org.json.JSONObject;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class FluorescenceMetadata extends MetadataBase {
    private final String filterBlock;
    private final Double exposure;
    private final Integer wavelength;
    
    public FluorescenceMetadata(MetadataBase base, String filterBlock, Double exposure, Integer wavelength) {
        super(base);
        this.filterBlock = filterBlock;
        this.exposure = exposure;
        this.wavelength = wavelength;
    }
    
    @Override
    public JSONObject toJson() {
        JSONObject md = super.toJson();
        try {
            md.put("filterBlock", filterBlock);
            md.put("exposure", exposure);
            if (wavelength == null) { md.put("wavelength", JSONObject.NULL); 
            } else { md.put("wavelength", wavelength); }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return md;
    }
}
