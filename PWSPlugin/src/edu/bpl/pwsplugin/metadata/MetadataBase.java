
package edu.bpl.pwsplugin.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.micromanager.data.internal.DefaultMetadata;
import org.micromanager.internal.propertymap.PropertyMapJSONSerializer;
import org.micromanager.internal.utils.ReportingUtils;


public class MetadataBase { //All images should have this metadata.
    private final List<Double> linearityPoly;
    private final String system;
    private final Integer darkCounts;
    private final String time;
    private final List<Double> affineTransform;
    private DefaultMetadata MMmd;
                    
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
        this.MMmd = base.MMmd;
    }
    
    public void setMicroManagerMetadata(DefaultMetadata md) { //This must be called before saving.
        this.MMmd = md;
    }
    
    /*public final List<Double> linearityPoly() { return linearityPoly; }
    public final String systemName() { return system; }
    public final Integer darkCounts() { return darkCounts; }*/
                
    public JsonObject toJson() {
        JsonObject md = new JsonObject();
        if (this.linearityPoly.size() > 0) {
            JsonArray linPoly = GsonUtils.getGson().toJsonTree(this.linearityPoly).getAsJsonArray();
            md.add("linearityPoly", linPoly);
        } else{
            md.add("linearityPoly", JsonNull.INSTANCE);
        }
        md.addProperty("system", this.system);
        md.addProperty("darkCounts", this.darkCounts);
        md.addProperty("time", this.time);
        md.add("cameraTransform", GsonUtils.getGson().toJsonTree(this.affineTransform).getAsJsonArray());
        if (MMmd == null) {
            throw new RuntimeException("Attempted to save metadata without adding micromanager metadata.");
        }
        md.add("MicroManagerMetadata", PropertyMapJSONSerializer.toGson(MMmd.toPropertyMap()).getAsJsonObject());
        return md;
    }
}
