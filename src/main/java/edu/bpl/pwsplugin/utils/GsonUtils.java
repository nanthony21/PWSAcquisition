///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;
import org.micromanager.StagePosition;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class GsonUtils {
   //This class provides custom code related to the use of GSON to convert objects to/from JSON.

   private static final GsonBuilder gsonBuilder = new GsonBuilder()
         .setPrettyPrinting() //This causes files to be saved in a more human-readable form.
         .registerTypeAdapterFactory(new MMTypeAdapterFactory())
         .serializeNulls(); //Without `serializeNulls` null fields will be skipped, then we json is loaded the default values will be used instead of null.

   public static GsonBuilder builder() {
      return gsonBuilder;
   }

   public static Gson getGson() { //This static method provides an easy way to get access to our custom instance of Gson.
      return gsonBuilder.create();
   }
}


class MMTypeAdapterFactory implements TypeAdapterFactory {
   //Type adapters for Micro-Manager classes. These specify how certain objects should be translated to/from JSON.
   //Even though Gson can automatically Jsonify these classes, by using these custom
   //TypeAdapters we protect a minor change in internal Micro-Manager code from breaking
   //all of our configuration files. E.G. if something about one of the classes changes
   //we would get errors when trying to load a configuration file. With these type adapter we will get a compile error here first.
   //These adapters could potentially be replaced by adding Gson's @SerializedName annotation to the fields of the classes.

   @Override
   //@SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (PositionList.class
            .isAssignableFrom(type.getRawType())) { //Allow subtypes to use this factory.
         return (TypeAdapter<T>) new PositionListTypeAdapter(gson);
      } else if (MultiStagePosition.class.isAssignableFrom(type.getRawType())) {
         return (TypeAdapter<T>) new MSPTypeAdapter(gson);
      } else if (StagePosition.class.isAssignableFrom(type.getRawType())) {
         return (TypeAdapter<T>) new StagePositionTypeAdapter(gson);
      }
      return null;
   }

}

class PositionListTypeAdapter extends TypeAdapter<PositionList> {

   //This custom adapter enables Steps to be Jsonified even though they have a circular parent/child reference.
   private final Gson gson;

   public PositionListTypeAdapter(Gson gson) {
      this.gson = gson;
   }

   @Override
   public void write(JsonWriter out, PositionList posList) throws IOException {
      out.beginObject();
      out.name("positions");
      out.beginArray();
      for (int i = 0; i < posList.getNumberOfPositions(); i++) {
         gson.toJson(posList.getPosition(i), MultiStagePosition.class, out);
      }
      out.endArray();
      out.endObject();
   }

   @Override
   public PositionList read(JsonReader in) throws IOException {
      in.beginObject();
      if (!in.nextName().equals("positions")) {
         throw new IOException("Json Parse Error");
      } //ID is determined at runtime don't load it.
      PositionList plist = new PositionList();
      in.beginArray();
      while (in.hasNext()) {
         MultiStagePosition msp = gson.fromJson(in, MultiStagePosition.class);
         plist.addPosition(msp);
      }
      in.endArray();
      in.endObject();
      return plist;
   }
}

class MSPTypeAdapter extends TypeAdapter<MultiStagePosition> {

   //This custom adapter enables Steps to be Jsonified even though they have a circular parent/child reference.
   private final Gson gson;

   public MSPTypeAdapter(Gson gson) {
      this.gson = gson;
   }

   @Override
   public void write(JsonWriter out, MultiStagePosition msp) throws IOException {
      out.beginObject();
      //private final ArrayList<StagePosition> stagePosList_;
      out.name("label");
      out.value(msp.getLabel());

      out.name("defaultZStage");
      out.value(msp.getDefaultZStage());
      out.name("defaultXYStage");
      out.value(msp.getDefaultXYStage());
      out.name("gridRow");
      out.value(msp.getGridRow());
      out.name("gridCol");
      out.value(msp.getGridColumn());
      out.name("stagePositions");
      out.beginArray();
      for (int i = 0; i < msp.size(); i++) {
         gson.toJson(msp.get(i), StagePosition.class, out);
      }
      out.endArray();
      out.endObject();
   }

   @Override
   public MultiStagePosition read(JsonReader in) throws IOException {
      in.beginObject();
      MultiStagePosition msp = new MultiStagePosition();
      in.nextName();// "label"
      msp.setLabel(in.nextString());
      in.nextName();// "defaultZStage"
      msp.setDefaultZStage(in.nextString());
      in.nextName();// "defaultXYStage"
      msp.setDefaultXYStage(in.nextString());
      in.nextName();// "gridRow"
      int row = in.nextInt();
      in.nextName();// "gridCol"
      int col = in.nextInt();
      msp.setGridCoordinates(row, col);
      in.nextName(); //stagePositions
      in.beginArray();
      while (in.hasNext()) {
         msp.add(gson.fromJson(in, StagePosition.class));
      }
      in.endArray();
      in.endObject();
      return msp;
   }
}

class StagePositionTypeAdapter extends TypeAdapter<StagePosition> {

   private final Gson gson;

   public StagePositionTypeAdapter(Gson gson) {
      this.gson = gson;
   }

   @Override
   public void write(JsonWriter out, StagePosition pos) throws IOException {
      out.beginObject();
      out.name("numAxes");
      out.value(pos.getNumberOfStageAxes());
      if (pos.getNumberOfStageAxes() == 2) {
         out.name("x");
         out.value(pos.get2DPositionX());
         out.name("y");
         out.value(pos.get2DPositionY());
      } else if (pos.getNumberOfStageAxes() == 1) {
         out.name("z");
         out.value(pos.get1DPosition());
      } else {
         throw new RuntimeException("Didn't account for this");
      }
      out.name("stageName");
      out.value(pos.getStageDeviceLabel());
      out.endObject();
   }

   @Override
   public StagePosition read(JsonReader in) throws IOException {
      in.beginObject();
      StagePosition pos;
      in.nextName(); // "numAxes"
      int numAxes = in.nextInt();
      if (numAxes == 1) {
         in.nextName(); // z
         double z = in.nextDouble();
         in.nextName(); // label
         String label = in.nextString();
         pos = StagePosition.create1D(label, z);
      } else if (numAxes == 2) {
         in.nextName(); // x
         double x = in.nextDouble();
         in.nextName(); // y
         double y = in.nextDouble();
         in.nextName(); // label
         String label = in.nextString();
         pos = StagePosition.create2D(label, x, y);
      } else {
         throw new RuntimeException();
      }
      in.endObject();
      return pos;
   }
}