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
public class FluorescenceMetadata extends MetadataBase {
    private String filterBlock;
    private Double exposure;
    
    public FluorescenceMetadata(MetadataBase base, String filterBlock, Double exposure) {
        super(base);
        this.filterBlock = filterBlock;
        this.exposure = exposure;
    }
    
    @Override
    public JSONObject toJson() {
        JSONObject md = super.toJson();
        try {
            md.put("filterBlock", filterBlock);
            md.put("exposure", exposure);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return md;
    }
}
