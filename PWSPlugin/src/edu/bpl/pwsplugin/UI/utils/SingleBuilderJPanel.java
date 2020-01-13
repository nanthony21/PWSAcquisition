
package edu.bpl.pwsplugin.UI.utils;

import edu.bpl.pwsplugin.utils.UIBuildable;
import java.awt.LayoutManager;
import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.micromanager.internal.utils.ReportingUtils;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class SingleBuilderJPanel<T extends UIBuildable> extends BuilderJPanel<T>{
    //This base class provides a convenient way to implement a UI that can be filled-in by,
    //and can construct a new instance of, T which is any UI buildable class.
    //Classes that extend this class simply need to implement a `getPropertyFieldMap` 
    //method which returns a Map<String, Object> where the String is the data member of
    //class T and the Object  is the corresponding UI component of the inheriting class.
    public SingleBuilderJPanel(LayoutManager layout, Class<T> clazz){
        super(layout, clazz);
    }
    
    protected abstract Map<String, Object> getPropertyFieldMap();
    
    @Override
    final public T build(){
        //Construct a new T from the UI components.
        T t = null;
        try {
            t = this.typeParamClass.newInstance();
            Map<String, Object> m = this.getPropertyFieldMap();
            for (Map.Entry<String, Object> entry: m.entrySet()) {
                Field prop = t.getClass().getField(entry.getKey());
                Object field = entry.getValue();

                if (field instanceof JTextField) {
                    prop.set(t, ((JTextField) field).getText());
                } else if (field instanceof JSpinner) {
                    prop.set(t, ((JSpinner) field).getValue());
                } else if (field instanceof JCheckBox) {
                    prop.set(t, ((JCheckBox) field).isSelected());
                } else if (field instanceof BuilderJPanel) {
                    prop.set(t, ((T)((BuilderJPanel<T>) field).build()));
                } else if (field instanceof DirectorySelector) {
                    prop.set(t, ((DirectorySelector) field).getText());
                } else if (field instanceof JCheckBox[]) { //from checkbox array to int.
                    boolean[] boolarr = new boolean[((JCheckBox[])field).length];
                    for (int i=0; i<boolarr.length; i++) {
                        boolarr[i] = ((JCheckBox[])field)[i].isSelected();
                    }
                    int n = 0, l = boolarr.length;
                    for (int i = 0; i < l; ++i) {
                        n = (n << 1) + (boolarr[i] ? 1 : 0);
                    }
                    prop.set(t, n);
                } else if (field instanceof JSpinner[]) {
                    int[] arr = new int[((JSpinner[])field).length];
                    for (int i=0; i<arr.length; i++) {
                        arr[i] = (int) ((JSpinner[]) field)[i].getValue();
                    }
                    prop.set(t, arr);
                } else if (field instanceof JComboBox) {
                    prop.set(t, ((JComboBox) field).getSelectedItem());
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
    public void populateFields(T t) throws Exception {
        //Fill in the the UI componenents based on the values of the data members
        //of `t`.
        Map<String, Object> m = this.getPropertyFieldMap();
        for (Map.Entry<String, Object> entry: m.entrySet()) {
            Field prop = t.getClass().getField(entry.getKey());
            Object field = entry.getValue();
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
            } else if (field instanceof JCheckBox[]) { //from int to jcheckbox[]
                int num = (int) prop.get(t);
                int l = ((JCheckBox[]) field).length;
                for (int i = 0; i < l; ++i) {
                    ((JCheckBox[]) field)[l-1-i].setSelected((num & (1 << i)) != 0);
                }
            } else if (field instanceof JSpinner[]) {
                int[] arr = (int[]) prop.get(t);
                for (int i=0; i<arr.length; i++) {
                    ((JSpinner[]) field)[i].setValue(arr[i]);
                }
            } else if (field instanceof JComboBox) {
                Object val = prop.get(t);
                ((JComboBox) field).setSelectedItem(val);
            }else {
                throw new UnsupportedOperationException("Build() does not support type: " + field.getClass().getName());
            }
        }
    }
}
