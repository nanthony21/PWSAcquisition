/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.fileSavers;

import org.json.JSONObject;

/**
 *
 * @author backman05
 */
public abstract class SaverThread extends Thread{
    public abstract void setMetadata(JSONObject md);
}
