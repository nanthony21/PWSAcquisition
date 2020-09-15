/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.metadata;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

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
    public JsonObject toJson() {
        JsonObject md = super.toJson();
        md.addProperty("filterBlock", filterBlock);
        md.addProperty("exposure", exposure);
        if (wavelength == null) {
            md.add("wavelength", JsonNull.INSTANCE); 
        } else { 
            md.addProperty("wavelength", wavelength);
        }
        return md;
    }
}
