
package edu.bpl.pwsplugin.UI.utils;

import java.awt.LayoutManager;
import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import edu.bpl.pwsplugin.UI.utils.ImprovedComponents.Spinner;
import javax.swing.JTextField;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public abstract class SingleBuilderJPanel<T> extends BuilderJPanel<T> {

   //This base class provides a convenient way to implement a UI that can be filled-in by,
   //and can construct a new instance of, T which is any UI buildable class.
   //Classes that extend this class simply need to implement a `getPropertyFieldMap`
   //method which returns a Map<String, Object> where the String is the data member of
   //class T and the Object  is the corresponding UI component of the inheriting class.
   public SingleBuilderJPanel(LayoutManager layout, Class<T> clazz) {
      super(layout, clazz);
   }

   protected abstract Map<String, Object> getPropertyFieldMap();

   @Override
   final public T build() throws BuilderPanelException {
      //Construct a new T from the UI components.
      T t = null;
      try {
         t = this.typeParamClass.newInstance();
         Map<String, Object> m = this.getPropertyFieldMap();
         for (Map.Entry<String, Object> entry : m.entrySet()) {
            Field prop = t.getClass().getField(entry.getKey());
            Object field = entry.getValue();

            if (field instanceof JTextField) {
               prop.set(t, ((JTextField) field).getText());
            } else if (field instanceof ImprovedComponents.Spinner) {
               prop.set(t, ((ImprovedComponents.Spinner) field).getValue());
            } else if (field instanceof JCheckBox) {
               prop.set(t, ((JCheckBox) field).isSelected());
            } else if (field instanceof BuilderJPanel) {
               prop.set(t, ((T) ((BuilderJPanel<T>) field).build()));
            } else if (field instanceof DirectorySelector) {
               prop.set(t, ((DirectorySelector) field).getText());
            } else if (field instanceof JComboBox) {
               prop.set(t, ((JComboBox) field).getSelectedItem());
            } else {
               throw new UnsupportedOperationException(
                     "Build() does not support type: " + field.getClass().getName());
            }
         }
      } catch (InstantiationException | IllegalAccessException | BuilderPanelException | NoSuchFieldException e) {
         throw new BuilderPanelException(e);
      }
      return t;
   }

   @Override
   public void populateFields(T t) throws BuilderPanelException {
      //Fill in the the UI componenents based on the values of the data members
      //of `t`.
      Map<String, Object> m = this.getPropertyFieldMap();
      for (Map.Entry<String, Object> entry : m.entrySet()) {
         Field prop;
         Object property;
         try {
            prop = t.getClass().getField(entry.getKey());
            property = prop.get(t);
         } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new BuilderPanelException(e);
         }
         if (property == null) {
            throw new BuilderPanelException(
                  String.format("Property %s of %s object is null", prop.getName(), t.toString()));
         }
         Object field = entry.getValue();
         if (field instanceof JTextField) {
            ((JTextField) field).setText((String) property);
         } else if (field instanceof ImprovedComponents.Spinner) {
            ((ImprovedComponents.Spinner) field).setValue(property);
         } else if (field instanceof JCheckBox) {
            ((JCheckBox) field).setSelected((boolean) property);
         } else if (field instanceof BuilderJPanel) {
            ((BuilderJPanel) field).populateFields(property);
         } else if (field instanceof DirectorySelector) {
            ((DirectorySelector) field).setText((String) property);
         } else if (field instanceof JComboBox) {
            Object val = property;
            ((JComboBox) field).setSelectedItem(val);
         } else {
            throw new BuilderPanelException(
                  "Build() does not support type: " + field.getClass().getName());
         }
      }
   }
}
