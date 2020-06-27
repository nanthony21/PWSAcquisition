/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bpl.pwsplugin.UI.utils;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.text.DefaultFormatter;

/**
 *
 * @author nicke
 */
public class ImprovedJSpinner extends JSpinner {
    //By default the JSpinner `getValue` does not update when a valid value is typed in unless focus is then changed to another component.
    //This class makes it so that the value updates as soon as a valid value is typed in. this fixes an annoying bug with many of th settings in the UI
    //where the user would think that they had set a value but actually it had been lost.
    
    public ImprovedJSpinner() {
        super();
        configureFormatter();
    }
    
    public ImprovedJSpinner(SpinnerModel model) {
        super(model);
        configureFormatter();
        
    }
    
    private void configureFormatter() {
        JFormattedTextField tf = ((JSpinner.DefaultEditor) this.getEditor()).getTextField();
        DefaultFormatter formatter = (DefaultFormatter) tf.getFormatter();
        formatter.setCommitsOnValidEdit(true); //This is the key line that make the value update when something is typed in.
    }
}
