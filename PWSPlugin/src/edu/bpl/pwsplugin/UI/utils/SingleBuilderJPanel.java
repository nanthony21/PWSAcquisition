
package edu.bpl.pwsplugin.UI.utils;

import java.awt.LayoutManager;
import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class SingleBuilderJPanel<T> extends BuilderJPanel<T>{
    
    public SingleBuilderJPanel(LayoutManager layout, Class<T> clazz){
        super(layout, clazz);
    }
    
    protected abstract Map<String, JComponent> getPropertyFieldMap();
    
    @Override
    final public T build(){
        T t = null;
        try {
            t = this.typeParamClass.newInstance();
            Map<String, JComponent> m = this.getPropertyFieldMap();
            for (Map.Entry<String, JComponent> entry: m.entrySet()) {
                Field prop = t.getClass().getField(entry.getKey());
                JComponent field = entry.getValue();

                if (field instanceof JTextField) {
                    prop.set(t, ((JTextField) field).getText());
                } else if (field instanceof JSpinner) {
                    prop.set(t, ((JSpinner) field).getValue());
                } else if (field instanceof JCheckBox) {
                    prop.set(t, ((JCheckBox) field).isSelected());
                } else if (field instanceof BuilderJPanel) {
                    prop.set(t, ((BuilderJPanel) field).build());
                } else if (field instanceof DirectorySelector) {
                    prop.set(t, ((DirectorySelector) field).getText());
                } else if (field instanceof PopulableJPanel) {
                    prop.set(t, ((PopulableJPanel) field).getStoredInstance());
                }else {
                    throw new UnsupportedOperationException("Build() does not support type: " + field.getClass().getName());
                }
            } 
        } catch (Exception e) {
            ReportingUtils.showError(e);
            ReportingUtils.logError(e);
        }
        return t;
    }
    
    @Override
    final public void populateFields(T t) {
        Map<String, JComponent> m = this.getPropertyFieldMap();
        try {
            for (Map.Entry<String, JComponent> entry: m.entrySet()) {
                Field prop = t.getClass().getField(entry.getKey());
                JComponent field = entry.getValue();
                if (field instanceof JTextField) {
                    ((JTextField) field).setText((String) prop.get(t));
                } else if (field instanceof JSpinner) {
                    ((JSpinner) field).setValue(prop.get(t));
                } else if (field instanceof JCheckBox) {
                    ((JCheckBox) field).setSelected((boolean) prop.get(t));
                } else if (field instanceof BuilderJPanel) {
                    ((BuilderJPanel) field).populateFields((UIBuildable) prop.get(t));
                } else if (field instanceof DirectorySelector) {
                    ((DirectorySelector) field).setText((String) prop.get(t));
                } else if (field instanceof PopulableJPanel) {
                    ((PopulableJPanel) field).storeInstance(prop.get(t));
                }else {
                    throw new UnsupportedOperationException("Build() does not support type: " + field.getName());
                }
            }
        } catch (Exception e) {
            ReportingUtils.showError(e);
            ReportingUtils.logError(e);
        }
    }
    
    /*public class Property {  The idea of this class is to provide a way for subclasses of BuilderJPanel to pass custom ways of setting GUI components from data and vice-versa.
        FunctionalInterface g_;
        FunctionalInterface s_;
        
        public Property(FunctionalInterface getter, FunctionalInterface setter) {
            g_ = getter;
            s_ = setter;
        }   
    }*/
}
