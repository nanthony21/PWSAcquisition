/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appliedmaterials.SGIPlugin.UI.util;

import java.awt.LayoutManager;
import com.appliedmaterials.SGIPlugin.util.UIBuildable;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class ListBuilderJPanel<T extends Iterable & UIBuildable> extends BuilderJPanel<T>{
    //A base class for a UI component representing a list of UIbuildable objects. Right now
    //this is only implemented by ListCardUI. other implementations are possible.
    public ListBuilderJPanel(LayoutManager layout, Class<T> clazz) {
        super(layout, clazz);
    }
}
