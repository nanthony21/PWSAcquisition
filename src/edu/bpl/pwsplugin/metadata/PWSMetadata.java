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
public class PWSMetadata extends MetadataBase{
    private final List<Double> wavelengths; //The wavelengths associated with each image in the image cube.
    private final Double exposure; //The exposure time that was used. milliseconds.
    
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
