/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class GsonUtils {

    private static final GsonBuilder gsonBuilder = new GsonBuilder()
            .setPrettyPrinting().serializeNulls(); //Without `serializeNulls` null fields will be skipped, then we json is loaded the default values will be used instead of null.

    public static void registerType(RuntimeTypeAdapterFactory<?> adapter) {
        gsonBuilder.registerTypeAdapterFactory(adapter);
    }

    public static Gson getGson() {
        return gsonBuilder.create();
    }
}
