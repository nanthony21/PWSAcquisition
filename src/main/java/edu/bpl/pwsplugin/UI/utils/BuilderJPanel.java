
package edu.bpl.pwsplugin.UI.utils;

import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * 
 * @author Nick Anthony <nickmanthony at hotmail.com>
 * @param <T> The class of the `settings` object.
 */
public abstract class BuilderJPanel<T> extends JPanel{
    //Base class for UI componenets that can `build` and be populated from an instance
    //of an UIbuildable class. This should probably just be an interface.
    protected Class<T> typeParamClass;

    public BuilderJPanel(LayoutManager layout, Class<T> clazz) {
        super(layout);
        this.typeParamClass = clazz;
    }
    
    public abstract T build() throws BuilderPanelException;
    public abstract void populateFields(T t) throws BuilderPanelException;
    
    public static class BuilderPanelException extends Exception {
        public BuilderPanelException() { super(); }
        public BuilderPanelException(Throwable cause) { super(cause); }
        public BuilderPanelException(String msg) { super(msg); }
    };
}
