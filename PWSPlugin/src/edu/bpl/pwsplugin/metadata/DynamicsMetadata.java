/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.metadata;

import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.util.List;

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
    public JsonObject toJson() {
        JsonObject md = super.toJson();
        md.addProperty("wavelength", wavelength);
        md.addProperty("exposure", exposure);
        md.add("times", GsonUtils.getGson().toJsonTree(times).getAsJsonArray());
        return md;
    }
}
