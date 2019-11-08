/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI;

import org.micromanager.PropertyMap;

/**
 *
 * @author nick
 */
public interface SaveableLoadableUI {
    public PropertyMap toSettings();
    public void fromSettings(PropertyMap map);
}
