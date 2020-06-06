/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class GsonUtils {

    private static final GsonBuilder gsonBuilder = new GsonBuilder()
            .registerTypeAdapter(DefaultMutableTreeNode.class, new DefaultMutableTreeNodeSerializer())
            .registerTypeAdapter(DefaultMutableTreeNode.class, new DefaultMutableTreeNodeDeserializer())
            .setPrettyPrinting()
            .serializeNulls(); //Without `serializeNulls` null fields will be skipped, then we json is loaded the default values will be used instead of null.

    public static void registerType(RuntimeTypeAdapterFactory<?> adapter) {
        gsonBuilder.registerTypeAdapterFactory(adapter);
    }

    public static Gson getGson() {
        return gsonBuilder.create();
    }
}

class DefaultMutableTreeNodeDeserializer implements JsonDeserializer<DefaultMutableTreeNode> {

    @Override
    public DefaultMutableTreeNode deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        return context.<POJO>deserialize(json, POJO.class).toDefaultMutableTreeNode();
    }

    private static class POJO {

        private boolean allowsChildren;
        private Object userObject;
        private List<POJO> children;
        // no need for: POJO parent

        public DefaultMutableTreeNode toDefaultMutableTreeNode() {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            node.setAllowsChildren(allowsChildren);
            node.setUserObject(userObject);
            if (children != null) {
                for (POJO child : children) {
                    node.add(child.toDefaultMutableTreeNode()); // recursion!
                    // this did also set the parent of the child-node
                }
            }
            return node;
        }

        // Following setters needed by Gson's deserialization:

        public void setAllowsChildren(boolean allowsChildren) {
            this.allowsChildren = allowsChildren;
        }

        public void setUserObject(Object userObject) {
            this.userObject = userObject;
        }

        public void setChildren(List<POJO> children) {
            this.children = children;
        }
    }
}

class DefaultMutableTreeNodeSerializer implements JsonSerializer<DefaultMutableTreeNode> {
    @Override
    public JsonElement serialize(DefaultMutableTreeNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("allowsChildren", src.getAllowsChildren());
        jsonObject.add("userObject", context.serialize(src.getUserObject()));
        if (src.getChildCount() > 0) {
            jsonObject.add("children", context.serialize(Collections.list(src.children())));
        }
        return jsonObject;
    }
}