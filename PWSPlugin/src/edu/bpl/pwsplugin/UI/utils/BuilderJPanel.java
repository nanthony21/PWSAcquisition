
package edu.bpl.pwsplugin.UI.utils;

import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class BuilderJPanel<T> extends JPanel{
    //Base class for UI componenets that can `build` and be populated from an instance
    //of an UIbuildable class. This should probably just be an interface.
    protected Class<T> typeParamClass;

    public BuilderJPanel(LayoutManager layout, Class<T> clazz) {
        super(layout);
        this.typeParamClass = clazz;
    }
    
    public abstract T build();
    public abstract void populateFields(T t);
}
