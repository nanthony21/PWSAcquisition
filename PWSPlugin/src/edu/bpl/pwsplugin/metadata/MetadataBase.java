
package edu.bpl.pwsplugin.metadata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import mmcorej.org.json.JSONArray;
import mmcorej.org.json.JSONException;
import mmcorej.org.json.JSONObject;
import org.micromanager.internal.utils.ReportingUtils;


public class MetadataBase { //All images should have this metadata.
    private final List<Double> linearityPoly;
    private final String system;
    private final Integer darkCounts;
    private final String time;
    private final List<Double> affineTransform;
                    
    public MetadataBase(List<Double> linearityPoly, String systemName, Integer darkCounts, List<Double> afTransform) {
        this.linearityPoly = linearityPoly;
        this.system = systemName;
        this.darkCounts = darkCounts;
        this.affineTransform = afTransform;
        this.time =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        if (this.system.equals("")) {
            ReportingUtils.showMessage("The `system` metadata field is blank. It should contain the name of the system.");
        }
        if (this.darkCounts.equals(0)) {
            ReportingUtils.showMessage("The `darkCounts` field of the metadata is 0. This can't be right.");
        }
        
    }
    
    protected MetadataBase(MetadataBase base) {
        this.linearityPoly = base.linearityPoly;
        this.system = base.system;
        this.darkCounts = base.darkCounts;
        this.affineTransform = base.affineTransform;
        this.time = base.time;
    }
    
    /*public final List<Double> linearityPoly() { return linearityPoly; }
    public final String systemName() { return system; }
    public final Integer darkCounts() { return darkCounts; }*/
                
    public JSONObject toJson() {
        try {
            JSONObject md = new JSONObject();
            if (this.linearityPoly.size() > 0) {
                JSONArray linPoly = new JSONArray(this.linearityPoly);
                md.put("linearityPoly", linPoly);
            } else{
                md.put("linearityPoly", JSONObject.NULL);
            }
            md.put("system", this.system);
            md.put("darkCounts", this.darkCounts);
            md.put("time", this.time);
            md.put("cameraTransform", new JSONArray(this.affineTransform));
            return md;
        } catch (JSONException e) {
            throw new RuntimeException(e); //This shouldn't happen.
        }
    }
}
