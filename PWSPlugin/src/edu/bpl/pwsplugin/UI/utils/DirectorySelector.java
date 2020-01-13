
package edu.bpl.pwsplugin.UI.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.function.Function;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.micromanager.internal.utils.FileDialogs;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public class DirectorySelector extends JPanel {
    //This is just a convenience class that combines a textField with a button
    //Used for selected directories. the DefaultMMFunctionFactory provides useful functions
    //for selecting MicroManager related files. Otherwise you need to provide a function to
    //the constructor. this function will be run when the button is clicked and will
    //return a String which will be filled in to the textField.
    
    private JTextField textField = new JTextField(20);
    private JButton browseButton = new JButton("...");
    private DirectorySelector This = this;
    
    public enum DefaultMMFunctions {
        MMAcquisitionSettings,
        MMDataSetDirectory
    }
    
    public class DefaultMMFunctionFactory {
        public Function<Void, File> getFunction(DefaultMMFunctions type) {
            if (type==DefaultMMFunctions.MMAcquisitionSettings) {
                return new Function<Void, File>() {
                    @Override
                    public File apply(Void v) {
                        FileDialogs.FileType ACQ_SETTINGS_FILE = new FileDialogs.FileType("ACQ_SETTINGS_FILE", "Acquisition settings",
                            System.getProperty("user.home") + "/AcqSettings.txt",
                            true, "txt");
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(This);
                        return FileDialogs.openFile(topFrame, "Load acquisition settings", ACQ_SETTINGS_FILE);
                    }
                };
            } else if (type==DefaultMMFunctions.MMDataSetDirectory) {
                return new Function<Void, File>() {
                    @Override
                    public File apply(Void v) {
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(This);
                        return FileDialogs.openDir(topFrame, "Dataset save location", FileDialogs.MM_DATA_SET);
                    }
                };
            } else {
                return null;
            }
        };
    }
    
    private DirectorySelector() {
        super();
        super.add(textField);
        super.add(browseButton);
    }
    
    public DirectorySelector(Function<Void, File> f) {
        this();
        ActionListener browseAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File result = f.apply(null);
                if (result!=null) {
                    textField.setText(result.getAbsolutePath());
                }
            }
        };
        browseButton.addActionListener(browseAction);
    }
    
    public DirectorySelector(DefaultMMFunctions defaultType) {
        this();
        Function<Void, File> func = new DefaultMMFunctionFactory().getFunction(defaultType);
        ActionListener browseAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File result = func.apply(null);
                if (result!=null) {
                    textField.setText(result.getAbsolutePath());
                }
            }
        };
        browseButton.addActionListener(browseAction);
    }
    
    public String getText() {
        return textField.getText();
    }
    
    public void setText(String str) {
        textField.setText(str);
    }
    
    @Override
    public void setEnabled(boolean state) {
        textField.setEnabled(state);
        browseButton.setEnabled(state);
    }
}
