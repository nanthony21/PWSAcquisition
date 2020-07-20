
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
    private final List<Double> linearityPoly; //A polynomial describing how to transform from camera counts to intensity. Most, but not all, cameras are linera so this isn't needed.
    private final String system; //the name of the system this acquisition was performed on.
    private final Integer darkCounts; //The darkcounts of the camera. this is the number measured when no binning is used.
    private final String time; //A datetime string indicating the time of acquisition.
    private final List<Double> affineTransform; //A 2x3 array indication the affine transform of the camera relative to the translation stage.
    private DefaultMetadata MMmd; //the micro-manager metadata object containing all sorts of other information.
                    
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
