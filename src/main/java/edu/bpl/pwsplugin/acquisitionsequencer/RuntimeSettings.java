package edu.bpl.pwsplugin.acquisitionsequencer;

import com.google.gson.Gson;
import edu.bpl.pwsplugin.acquisitionsequencer.steps.Step;
import edu.bpl.pwsplugin.utils.GsonUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class RuntimeSettings {
   private final String dateString = new Timestamp(new Date().getTime()).toString();
   private final String uuid = UUID.randomUUID().toString();
   private final Step rootStep;

   public RuntimeSettings(Step root) {
      rootStep = root;
   }

   public String getUUID() {
      return uuid;
   }

   public Step getRootStep() {
      return rootStep;
   }

   public void saveToJson(String directory) throws IOException {
      String savePath = Paths.get(directory, "sequence.rtpwsseq").toString();
      try (FileWriter writer = new FileWriter(savePath)) {
         Gson gson = GsonUtils.getGson();
         String json = gson.toJson(this);
         writer.write(json);
      }
   }
}
