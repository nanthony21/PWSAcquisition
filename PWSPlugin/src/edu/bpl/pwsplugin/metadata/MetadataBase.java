
package edu.bpl.pwsplugin.metadata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import mmcorej.org.json.JSONArray;
import mmcorej.org.json.JSONException;
import mmcorej.org.json.JSONObject;
import org.micromanager.internal.utils.ReportingUtils;


public class MetadataBase {
    private final List<Double> linearityPoly;
    private final String system;
    private final Integer darkCounts;
    private final String time;
    private final List<Double> affineTransform;
                    
    private MetadataBase(List<Double> linearityPoly, String systemName, Integer darkCounts, List<Double> afTransform) {
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
    
    public MetadataBase(MetadataBase base) {
        this.linearityPoly = base.linearityPoly;
        this.system = base.system;
        this.darkCounts = base.darkCounts;
        this.affineTransform = base.affineTransform;
        this.time = base.time;
    }
            
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
            return md;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class Builder {
        private List<Double> linearityPoly;
        private String systemName;
        private Integer darkCounts;
        private List<Double> affineTransform;

        public Builder() {
        }

        public Builder linearityPoly(List<Double> linearityPoly) {
            this.linearityPoly = linearityPoly;
            return this;
        }

        public Builder systemName(String systemName) {
            this.systemName = systemName;
            return this;
        }

        public Builder darkCounts(Integer darkCounts) {
            this.darkCounts = darkCounts;
            return this;
        }
        
        public Builder affineTransform(List<Double> trans) {
            this.affineTransform = trans;
            return this;
        }

        public MetadataBase build() { //TODO make each field required.
            return new MetadataBase(linearityPoly, systemName, darkCounts, affineTransform);
        }

    }
}
